CREATE TABLE blocks (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  timestamp     BIGINT  NOT NULL,
  height        BIGINT  NOT NULL,
  previous_hash text NOT NULL,
  `hash`        varchar(512) NOT NULL,
  signature     text NOT NULL,
  public_key    text NOT NULL
);
--
CREATE UNIQUE INDEX blocks_hash
  ON blocks (hash);
--
CREATE UNIQUE INDEX blocks_height
  ON blocks (height);
--

CREATE TABLE main_blocks (
  id                       BIGINT  PRIMARY KEY REFERENCES blocks,
  transaction_merkle_hash  text NOT NULL,
  state_merkle_hash        text NOT NULL,
  receipt_merkle_hash      text NOT NULL
);
--
CREATE TABLE genesis_blocks (
  id          BIGINT PRIMARY KEY REFERENCES blocks,
  epoch_index BIGINT NOT NULL
);
--
CREATE INDEX genesis_blocks_epoch_index
  ON genesis_blocks (epoch_index);
--
CREATE TABLE delegate2genesis (
  public_key  varchar(512) NOT NULL,
  genesis_id  BIGINT NOT NULL REFERENCES genesis_blocks,
  PRIMARY KEY (public_key, genesis_id)
);