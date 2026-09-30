-- O id do entregador é o mesmo id do usuário no auth-service.
CREATE TABLE drivers (
    id                  BIGINT PRIMARY KEY,
    status              VARCHAR(10) NOT NULL DEFAULT 'OFFLINE',
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    location_updated_at TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE deliveries (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT         NOT NULL UNIQUE,
    customer_id       BIGINT         NOT NULL,
    restaurant_id     BIGINT         NOT NULL,
    restaurant_name   VARCHAR(120)   NOT NULL,
    pickup_address    VARCHAR(300),
    pickup_latitude   DOUBLE PRECISION,
    pickup_longitude  DOUBLE PRECISION,
    dropoff_address   VARCHAR(300)   NOT NULL,
    dropoff_latitude  DOUBLE PRECISION,
    dropoff_longitude DOUBLE PRECISION,
    driver_fee        NUMERIC(10, 2) NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    driver_id         BIGINT REFERENCES drivers (id),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    accepted_at       TIMESTAMPTZ,
    picked_up_at      TIMESTAMPTZ,
    delivered_at      TIMESTAMPTZ,
    version           BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_deliveries_status ON deliveries (status);
CREATE INDEX idx_deliveries_driver ON deliveries (driver_id, status);
