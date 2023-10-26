CREATE TABLE blocks (
  id            INTEGER PRIMARY KEY,
  timestamp     INTEGER  NOT NULL,
  height        INTEGER  NOT NULL,
  previous_hash VARCHAR NOT NULL,
  hash          VARCHAR NOT NULL,
  signature     VARCHAR NOT NULL,
  public_key    VARCHAR NOT NULL
);
--
CREATE UNIQUE INDEX blocks_hash
  ON blocks (hash);
--
CREATE UNIQUE INDEX blocks_height
  ON blocks (height);
--

CREATE TABLE main_blocks (
  id                       INTEGER  PRIMARY KEY REFERENCES blocks,
  transaction_merkle_hash  VARCHAR NOT NULL,
  state_merkle_hash        VARCHAR NOT NULL,
  receipt_merkle_hash      VARCHAR NOT NULL
);
--
CREATE TABLE genesis_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  epoch_index INTEGER NOT NULL
);
--
CREATE INDEX genesis_blocks_epoch_index
  ON genesis_blocks (epoch_index);
--
CREATE TABLE delegate2genesis (
  public_key     VARCHAR NOT NULL,
  genesis_id  INTEGER NOT NULL REFERENCES genesis_blocks,
  PRIMARY KEY (public_key, genesis_id)
);