CREATE TABLE thermostats (
    device_id           VARCHAR(64) PRIMARY KEY,
    home_id             VARCHAR(64)      NOT NULL,
    target_temperature  DOUBLE PRECISION NOT NULL,
    mode                VARCHAR(16)      NOT NULL,
    current_temperature DOUBLE PRECISION,
    relay_on            BOOLEAN,
    online              BOOLEAN          NOT NULL DEFAULT FALSE,
    last_seen_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ      NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_thermostats_home ON thermostats (home_id) WHERE deleted_at IS NULL;

CREATE TABLE processed_events (
    event_id     VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
