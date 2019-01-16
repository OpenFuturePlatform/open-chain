CREATE TABLE states (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  address      VARCHAR NOT NULL,
  data         VARCHAR NOT NULL,
  height_block BIGINT  NOT NULL,
  UNIQUE (address, height_block)
);