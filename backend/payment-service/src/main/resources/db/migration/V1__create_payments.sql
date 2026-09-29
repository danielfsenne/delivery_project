CREATE TABLE payments (
    id                 BIGSERIAL PRIMARY KEY,
    order_id           BIGINT         NOT NULL,
    customer_id        BIGINT         NOT NULL,
    amount             NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    method             VARCHAR(20)    NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    provider           VARCHAR(40)    NOT NULL,
    provider_reference VARCHAR(100),
    failure_reason     VARCHAR(200),
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_order ON payments (order_id);

-- Garante no máximo um pagamento aprovado por pedido, mesmo com requisições concorrentes.
CREATE UNIQUE INDEX uq_payments_order_approved ON payments (order_id) WHERE status = 'APPROVED';
