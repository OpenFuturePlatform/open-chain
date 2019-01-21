CREATE TABLE blocks (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  timestamp     BIGINT  NOT NULL,
  height        BIGINT  NOT NULL,
  previous_hash VARCHAR NOT NULL,
  hash          VARCHAR NOT NULL UNIQUE,
  signature     VARCHAR NOT NULL,
  public_key    VARCHAR NOT NULL
);
--
CREATE INDEX block_height
  ON blocks (height);
--
CREATE INDEX block_hash
  ON blocks (hash);
--
CREATE TABLE main_blocks (
  id          BIGINT PRIMARY KEY REFERENCES blocks,
  merkle_hash VARCHAR NOT NULL
);
--
CREATE TABLE genesis_blocks (
  id          BIGINT PRIMARY KEY REFERENCES blocks,
  epoch_index BIGINT NOT NULL
);
CREATE INDEX genesis_blocks_epoch_index
  ON genesis_blocks (epoch_index);
--