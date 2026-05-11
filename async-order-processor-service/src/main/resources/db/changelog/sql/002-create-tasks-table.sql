-- Liquibase formatted sql
-- changeset create-tasks-table:002

CREATE TABLE IF NOT EXISTS tasks
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        UUID        NOT NULL,
    status          INTEGER     NOT NULL,
    attempts        INTEGER,
    next_attempt_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tasks_order_id ON tasks (order_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status_next_attempt ON tasks (status, next_attempt_at);