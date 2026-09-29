CREATE TABLE coupons (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(30)    NOT NULL UNIQUE,
    description     VARCHAR(200),
    type            VARCHAR(20)    NOT NULL,
    value           NUMERIC(10, 2) NOT NULL CHECK (value > 0),
    min_order_value NUMERIC(10, 2) NOT NULL DEFAULT 0,
    max_discount    NUMERIC(10, 2),
    valid_until     TIMESTAMPTZ,
    usage_limit     INT,
    used_count      INT            NOT NULL DEFAULT 0,
    restaurant_id   BIGINT,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT chk_percentage CHECK (type <> 'PERCENTAGE' OR value <= 100),
    CONSTRAINT chk_usage CHECK (usage_limit IS NULL OR used_count <= usage_limit)
);

ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(30);

INSERT INTO coupons (code, description, type, value, min_order_value, max_discount, valid_until, usage_limit)
VALUES ('SAVE10', '10% de desconto em pedidos a partir de R$ 50 (máx. R$ 20)', 'PERCENTAGE', 10, 50, 20,
        '2027-12-31T23:59:59-03:00', NULL),
       ('BEMVINDO', 'R$ 15 de desconto em pedidos a partir de R$ 40', 'FIXED', 15, 40, NULL,
        '2027-12-31T23:59:59-03:00', 1000);

INSERT INTO coupons (code, description, type, value, min_order_value, max_discount, valid_until, restaurant_id)
VALUES ('PIZZA20', '20% no Forno da Nonna (máx. R$ 30)', 'PERCENTAGE', 20, 0, 30, '2027-12-31T23:59:59-03:00', 3);
