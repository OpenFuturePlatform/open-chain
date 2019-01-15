CREATE TABLE states (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    address  VARCHAR NOT NULL,
    data     VARCHAR,
    block_id   BIGINT REFERENCES blocks(id)
);