-- Agregado de avaliações mantido pelo order-service via endpoint interno.
ALTER TABLE restaurants
    ADD COLUMN rating_sum   BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN rating_count INT    NOT NULL DEFAULT 0;
