-- Transactional Outbox: eventos gravados na mesma transação da mudança e publicados pelo relay.
CREATE TABLE outbox_events (
    id           BIGSERIAL PRIMARY KEY,
    event_id     UUID         NOT NULL UNIQUE,
    routing_key  VARCHAR(100) NOT NULL,
    event_type   VARCHAR(200) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ,
    attempts     INT          NOT NULL DEFAULT 0,
    last_error   VARCHAR(500)
);

CREATE INDEX idx_outbox_pending ON outbox_events (id) WHERE published_at IS NULL;

-- Eventos já consumidos, para descartar reentregas.
CREATE TABLE processed_events (
    event_id     UUID         NOT NULL,
    consumer     VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer)
);
