CREATE TABLE blocks (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY HASH,
  timestamp     BIGINT  NOT NULL,
  height        BIGINT  NOT NULL,
  previous_hash VARCHAR NOT NULL,
  hash          VARCHAR NOT NULL,
  signature     VARCHAR NOT NULL,
  public_key    VARCHAR NOT NULL
);
--
CREATE UNIQUE HASH INDEX blocks_hash
  ON blocks (hash);
--
CREATE UNIQUE HASH INDEX blocks_height
  ON blocks (height);
--

CREATE TABLE main_blocks (
  id                       BIGINT  PRIMARY KEY REFERENCES blocks,
  transaction_merkle_hash  VARCHAR NOT NULL,
  state_merkle_hash        VARCHAR NOT NULL,
  receipt_merkle_hash      VARCHAR NOT NULL
);
--
CREATE TABLE genesis_blocks (
  id          BIGINT PRIMARY KEY REFERENCES blocks,
  epoch_index BIGINT NOT NULL
);
--
CREATE HASH INDEX genesis_blocks_epoch_index
  ON genesis_blocks (epoch_index);
--
CREATE TABLE delegate2genesis (
  public_key     VARCHAR NOT NULL,
  genesis_id  BIGINT NOT NULL REFERENCES genesis_blocks,
  PRIMARY KEY (public_key, genesis_id)
);