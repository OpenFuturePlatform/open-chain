CREATE TABLE delegates (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY HASH,
  public_key        VARCHAR NOT NULL,
  address           VARCHAR NOT NULL,
  registration_date BIGINT  NOT NULL
);
--
CREATE UNIQUE HASH INDEX delegates_public_key
  ON delegates (public_key);
--
CREATE TABLE delegate2genesis (
  public_key     VARCHAR NOT NULL,
  genesis_id  BIGINT NOT NULL REFERENCES genesis_blocks,
  PRIMARY KEY (public_key, genesis_id)
);