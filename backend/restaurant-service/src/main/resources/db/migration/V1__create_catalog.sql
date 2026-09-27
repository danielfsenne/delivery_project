CREATE TABLE restaurants (
    id                     BIGSERIAL PRIMARY KEY,
    owner_id               BIGINT        NOT NULL,
    name                   VARCHAR(120)  NOT NULL,
    description            VARCHAR(500),
    cuisine                VARCHAR(60)   NOT NULL,
    phone                  VARCHAR(20),
    image_url              VARCHAR(500),
    delivery_fee           NUMERIC(10, 2) NOT NULL,
    min_order_value        NUMERIC(10, 2) NOT NULL DEFAULT 0,
    delivery_time_min      INT           NOT NULL,
    delivery_time_max      INT           NOT NULL,
    active                 BOOLEAN       NOT NULL DEFAULT TRUE,
    street                 VARCHAR(160)  NOT NULL,
    number                 VARCHAR(20)   NOT NULL,
    complement             VARCHAR(80),
    district               VARCHAR(80)   NOT NULL,
    city                   VARCHAR(80)   NOT NULL,
    state                  VARCHAR(2)    NOT NULL,
    zip_code               VARCHAR(9)    NOT NULL,
    latitude               DOUBLE PRECISION,
    longitude              DOUBLE PRECISION,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_restaurants_owner ON restaurants (owner_id);
CREATE INDEX idx_restaurants_city ON restaurants (lower(city));

CREATE TABLE opening_hours (
    restaurant_id BIGINT     NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    day_of_week   VARCHAR(9) NOT NULL,
    opens_at      TIME       NOT NULL,
    closes_at     TIME       NOT NULL
);

CREATE INDEX idx_opening_hours_restaurant ON opening_hours (restaurant_id);

CREATE TABLE categories (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT       NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    name          VARCHAR(80)  NOT NULL,
    position      INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_categories_restaurant ON categories (restaurant_id);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT         NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    name        VARCHAR(120)   NOT NULL,
    description VARCHAR(500),
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    image_url   VARCHAR(500),
    available   BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_products_category ON products (category_id);

CREATE TABLE product_options (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    name       VARCHAR(80)    NOT NULL,
    price      NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (price >= 0)
);

CREATE INDEX idx_product_options_product ON product_options (product_id);
