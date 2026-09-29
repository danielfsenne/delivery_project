-- Endereço de retirada (restaurante) copiado no momento do pedido, usado pelo serviço de entrega.
ALTER TABLE orders
    ADD COLUMN pickup_address   VARCHAR(300),
    ADD COLUMN pickup_latitude  DOUBLE PRECISION,
    ADD COLUMN pickup_longitude DOUBLE PRECISION;

CREATE TABLE reviews (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT       NOT NULL UNIQUE REFERENCES orders (id),
    customer_id     BIGINT       NOT NULL,
    restaurant_id   BIGINT       NOT NULL,
    driver_id       BIGINT,
    food_rating     INT          NOT NULL CHECK (food_rating BETWEEN 1 AND 5),
    delivery_rating INT          CHECK (delivery_rating BETWEEN 1 AND 5),
    comment         VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_reviews_restaurant ON reviews (restaurant_id, created_at DESC);
CREATE INDEX idx_reviews_driver ON reviews (driver_id);
