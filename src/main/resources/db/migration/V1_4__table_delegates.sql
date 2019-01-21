CREATE TABLE delegates (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  public_key        VARCHAR NOT NULL UNIQUE,
  address           VARCHAR NOT NULL,
  host              VARCHAR NOT NULL,
  port              INTEGER NOT NULL,
  registration_date BIGINT  NOT NULL
);

CREATE TABLE delegate2genesis (
  public_key     VARCHAR NOT NULL,
  genesis_id  BIGINT NOT NULL REFERENCES genesis_blocks,
  PRIMARY KEY (public_key, genesis_id)
);