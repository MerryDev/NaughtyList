CREATE TABLE IF NOT EXISTS player
(
    uuid            UUID      NOT NULL PRIMARY KEY,
    discord_id      TEXT UNIQUE,
    first_joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS player_name_history
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    player_uuid UUID        NOT NULL REFERENCES player (uuid) ON DELETE CASCADE,
    name        VARCHAR(16) NOT NULL,
    valid_from  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP,
    CHECK ( valid_until IS NULL OR valid_until > valid_from )
);

CREATE UNIQUE INDEX uq_player_current_name ON player_name_history (player_uuid) WHERE valid_until IS NULL;
CREATE INDEX idx_player_name_history_name ON player_name_history (LOWER(name));

CREATE TYPE case_status AS ENUM ('OPEN', 'CLOSED', 'DISMISSED');

CREATE TABLE IF NOT EXISTS moderation_case
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    target_uuid UUID        NOT NULL REFERENCES player (uuid),
    status      CASE_STATUS NOT NULL DEFAULT 'OPEN',
    title       TEXT,
    summary     TEXT,
    created_by  UUID REFERENCES player (uuid),
    assigned_to UUID REFERENCES player (uuid),
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at   TIMESTAMP,
    UNIQUE (target_uuid, id),
    CHECK ((status = 'OPEN' AND closed_at IS NULL) OR (status IN ('CLOSED', 'DISMISSED') AND closed_at IS NOT NULL))
);

CREATE INDEX idx_open_cases ON moderation_case (created_at) WHERE status = 'OPEN';
CREATE INDEX idx_case_target on moderation_case (target_uuid, created_at DESC);

CREATE TYPE evidence_type AS ENUM ('REPLAY', 'SCREENSHOT', 'VIDEO', 'CHAT_LOG');

CREATE TABLE IF NOT EXISTS case_evidence
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    case_id    BIGINT        NOT NULL REFERENCES moderation_case (id),
    added_by   UUID          NOT NULL REFERENCES player (uuid),
    type       EVIDENCE_TYPE NOT NULL,
    value      TEXT          NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS case_note
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    case_id     BIGINT    NOT NULL REFERENCES moderation_case (id),
    author_uuid UUID      NOT NULL REFERENCES player (uuid),
    content     TEXT      NOT NULL CHECK (LENGTH(TRIM(content)) > 0),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reason
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    key         TEXT      NOT NULL UNIQUE,
    name        TEXT      NOT NULL,
    description TEXT,
    enabled     BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE report_status AS ENUM ('OPEN', 'ACCEPTED', 'CLOSED');

CREATE TABLE IF NOT EXISTS report
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    replay_id     TEXT UNIQUE,
    server_name   TEXT          NOT NULL,
    reporter_uuid UUID          NOT NULL REFERENCES player (uuid),
    target_uuid   UUID          NOT NULL REFERENCES player (uuid),
    reason_id     BIGINT        NOT NULL REFERENCES reason (id),
    status        REPORT_STATUS NOT NULL DEFAULT 'OPEN',
    case_id       BIGINT        REFERENCES moderation_case (id) ON DELETE SET NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_uuid, case_id) REFERENCES moderation_case (target_uuid, id)
);

CREATE INDEX idx_open_reports ON report (created_at) WHERE status = 'OPEN';

CREATE TYPE punishment_type AS ENUM ('WARNING', 'MUTE', 'BAN');

CREATE TABLE IF NOT EXISTS escalation_policy
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    key         TEXT      NOT NULL UNIQUE,
    name        TEXT      NOT NULL,
    description TEXT,
    reason_id   BIGINT REFERENCES reason (id), -- NULL = globale Policy über gesamte Punishment-History
    enabled     BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS escalation_policy_version
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_id  BIGINT    NOT NULL REFERENCES escalation_policy (id) ON DELETE CASCADE,
    version    INTEGER   NOT NULL CHECK (version >= 1),
    lookback   INTERVAL, -- NULL = gesamte History betrachten
    active     BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (policy_id, version)
);

CREATE UNIQUE INDEX uq_active_escalation_policy_version ON escalation_policy_version (policy_id) WHERE active = TRUE;

CREATE TABLE IF NOT EXISTS escalation_step
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_version_id BIGINT          NOT NULL REFERENCES escalation_policy_version (id) ON DELETE CASCADE,
    minimum_offenses  INTEGER         NOT NULL CHECK (minimum_offenses >= 1),
    type              PUNISHMENT_TYPE NOT NULL,
    duration          INTERVAL, --NULL = keine Dauer / permanent bei BAN bzw. MUTE
    UNIQUE (policy_version_id, minimum_offenses),
    UNIQUE (policy_version_id, id)
);

CREATE TABLE IF NOT EXISTS punishment
(
    id                           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type                         PUNISHMENT_TYPE NOT NULL,
    player_uuid                  UUID            NOT NULL REFERENCES player (uuid),
    reason_id                    BIGINT          NOT NULL REFERENCES reason (id),
    case_id                      BIGINT          REFERENCES moderation_case (id) ON DELETE SET NULL,
    issued_by                    UUID            NOT NULL REFERENCES player (uuid),
    escalation_policy_version_id BIGINT REFERENCES escalation_policy_version (id), -- NULL, wenn die Strafe nicht über eine Eskalation entstanden ist
    escalation_step_id           BIGINT REFERENCES escalation_step (id),           -- NULL, wenn kein konkreter Eskalationsschritt verwendet wurde
    starts_at                    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at                   TIMESTAMP,                                        -- NULL = permanent
    revoked_at                   TIMESTAMP,
    revoked_by                   UUID REFERENCES player (uuid),
    revoke_reason                TEXT,
    override_reason              TEXT,
    created_at                   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (player_uuid, id),
    FOREIGN KEY (player_uuid, case_id) REFERENCES moderation_case (target_uuid, id),
    FOREIGN KEY (escalation_policy_version_id, escalation_step_id) REFERENCES escalation_step (policy_version_id, id),
    CHECK (expires_at IS NULL OR expires_at > starts_at),
    CHECK ((revoked_at IS NULL AND revoked_by IS NULL) OR (revoked_at IS NOT NULL AND revoked_by IS NOT NULL)),
    CHECK ((escalation_policy_version_id IS NULL AND escalation_step_id IS NULL) OR (escalation_policy_version_id IS NOT NULL AND escalation_step_id IS NOT NULL))
);

CREATE INDEX idx_punishment_player_type ON punishment (player_uuid, type, expires_at);
CREATE INDEX idx_active_bans ON punishment (player_uuid, expires_at) WHERE type = 'BAN' AND revoked_at IS NULL;
CREATE INDEX idx_active_mutes ON punishment (player_uuid, expires_at) WHERE type = 'MUTE' AND revoked_at IS NULL;

CREATE TYPE appeal_status AS ENUM ('OPEN', 'ACCEPTED', 'REJECTED');

CREATE TABLE IF NOT EXISTS appeal
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    punishment_id BIGINT        NOT NULL REFERENCES punishment (id),
    player_uuid   UUID          NOT NULL REFERENCES player (uuid),
    status        APPEAL_STATUS NOT NULL DEFAULT 'OPEN',
    message       TEXT          NOT NULL CHECK (LENGTH(TRIM(message)) > 0),
    reviewed_by   UUID REFERENCES player (uuid),
    reviewed_at   TIMESTAMP,
    response      TEXT,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_uuid, punishment_id) REFERENCES punishment (player_uuid, id),
    CHECK (reviewed_at IS NULL OR reviewed_at >= created_at),
    CHECK ((status = 'OPEN' AND reviewed_by IS NULL AND reviewed_at IS NULL) OR (status IN ('ACCEPTED', 'REJECTED') AND reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL))
);

CREATE UNIQUE INDEX uq_open_appeal_per_punishment ON appeal (punishment_id) WHERE status = 'OPEN';

CREATE TABLE IF NOT EXISTS audit_log
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    actor_uuid  UUID      REFERENCES player (uuid) ON DELETE SET NULL,
    action      TEXT      NOT NULL,
    entity_type TEXT      NOT NULL,
    entity_id   BIGINT,
    case_id     BIGINT    REFERENCES moderation_case (id) ON DELETE SET NULL,
    data        JSONB     NOT NULL DEFAULT '{}'::JSONB,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

----------------------------------------------------------------------
-- Funktionen / Trigger
----------------------------------------------------------------------

CREATE OR REPLACE FUNCTION validate_punishment_override() RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
DECLARE
    step_type       PUNISHMENT_TYPE;
    step_duration   INTERVAL;
    actual_duration INTERVAL;
    is_override     BOOLEAN;

BEGIN
    -- Manuelles Punishment ohne Escalation Policy
    IF NEW.escalation_step_id IS NULL THEN
        IF NEW.override_reason IS NOT NULL THEN
            RAISE EXCEPTION 'override_reason must be null when no escalation step is used';
        END IF;

        RETURN NEW;
    END IF;

    -- Laden vom verwendeten Eskalationsschritt
    SELECT type, duration
    INTO step_type, step_duration
    FROM escalation_step
    WHERE id = NEW.escalation_step_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Escalation step % does not exist', NEW.escalation_step_id;
    END IF;

    -- Tatsächliche Dauer des Punishments berechnen
    IF NEW.expires_at IS NULL THEN
        actual_duration := NULL;
    ELSE
        actual_duration := NEW.expires_at - NEW.starts_at;
    END IF;

    -- Prüfen, ob Type oder Dauer vom Schritt abweichen
    is_override := NEW.type IS DISTINCT FROM step_type OR actual_duration IS DISTINCT FROM step_duration;

    -- Override ist vorhanden, aber kein Grund angegeben
    IF is_override AND (NEW.override_reason IS NULL OR LENGTH(TRIM(NEW.override_reason)) = 0) THEN
        RAISE EXCEPTION 'override_reason is required when punishment differs from escalation step';
    END IF;

    -- Kein Override, aber trotzdem override_reason angegeben
    IF NOT is_override AND NEW.override_reason IS NOT NULL THEN
        RAISE EXCEPTION 'override_reason must be null when punishment matches escalation step';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validate_punishment_override
    BEFORE INSERT OR UPDATE
    ON punishment
    FOR EACH ROW
EXECUTE FUNCTION validate_punishment_override();