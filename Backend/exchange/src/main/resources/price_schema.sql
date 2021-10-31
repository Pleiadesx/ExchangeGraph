CREATE TABLE IF NOT EXISTS prices
(
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    price_cents     BIGINT          NOT NULL,
    epoch           BIGINT          NOT NULL,
    coin_type       VARCHAR(16)     NOT NULL,
    exchange_id     VARCHAR(16)     NOT NULL
    -- ideally exchange_id would be a marker/key onto another table
    -- containing exchanges and their info but right now using a more
    -- simple approach

);