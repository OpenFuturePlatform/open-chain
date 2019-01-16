CREATE TABLE states (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  address  VARCHAR NOT NULL,
  data     VARCHAR NOT NULL,
  block_id BIGINT REFERENCES blocks (id),
  UNIQUE (address, block_id)
);