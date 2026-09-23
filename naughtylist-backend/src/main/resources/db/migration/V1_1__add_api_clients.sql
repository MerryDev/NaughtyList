CREATE TABLE api_client
(
    client_id   TEXT PRIMARY KEY,
    name        TEXT      NOT NULL,
    secret_hash TEXT      NOT NULL,
    enabled     BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);