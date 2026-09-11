ALTER TABLE report
    DROP CONSTRAINT IF EXISTS report_replay_id_key;

CREATE INDEX idx_report_server_replay ON report (server_name, replay_id) WHERE replay_id IS NOT NULL;