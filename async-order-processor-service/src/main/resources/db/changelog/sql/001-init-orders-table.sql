-- changeset init-orders-:001

CREATE TABLE IF NOT EXISTS orders
(
    -- uses built-in UUID v7 function available in Postgres 18+
    id                  UUID PRIMARY KEY DEFAULT uuidv7(),
    status              INTEGER     NOT NULL,
    address             TEXT,
    description         TEXT,
    reserved            BOOLEAN,
    price               NUMERIC(12, 2),
    eta_days            INTEGER,
    cancellation_reason TEXT,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
