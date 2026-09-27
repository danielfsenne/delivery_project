CREATE TABLE orders (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT         NOT NULL,
    restaurant_id       BIGINT         NOT NULL,
    restaurant_name     VARCHAR(120)   NOT NULL,
    restaurant_owner_id BIGINT         NOT NULL,
    driver_id           BIGINT,
    status              VARCHAR(30)    NOT NULL,
    payment_method      VARCHAR(20)    NOT NULL,
    subtotal            NUMERIC(10, 2) NOT NULL,
    delivery_fee        NUMERIC(10, 2) NOT NULL,
    discount            NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total               NUMERIC(10, 2) NOT NULL,
    notes               VARCHAR(300),
    street              VARCHAR(160)   NOT NULL,
    number              VARCHAR(20)    NOT NULL,
    complement          VARCHAR(80),
    district            VARCHAR(80)    NOT NULL,
    city                VARCHAR(80)    NOT NULL,
    state               VARCHAR(2)     NOT NULL,
    zip_code            VARCHAR(9)     NOT NULL,
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version             BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_orders_customer ON orders (customer_id, created_at DESC);
CREATE INDEX idx_orders_restaurant ON orders (restaurant_id, created_at DESC);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id  BIGINT         NOT NULL,
    name        VARCHAR(120)   NOT NULL,
    options     VARCHAR(500),
    notes       VARCHAR(200),
    unit_price  NUMERIC(10, 2) NOT NULL,
    quantity    INT            NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);

-- Auditoria de cada transição de status do pedido.
CREATE TABLE order_history (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT      NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    event       VARCHAR(40) NOT NULL,
    old_status  VARCHAR(30),
    new_status  VARCHAR(30) NOT NULL,
    user_id     BIGINT,
    reason      VARCHAR(300),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_history_order ON order_history (order_id, created_at);
