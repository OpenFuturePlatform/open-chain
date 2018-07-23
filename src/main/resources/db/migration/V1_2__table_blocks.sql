CREATE TABLE blocks (
  id            INTEGER PRIMARY KEY,
  hash          VARCHAR NOT NULL,
  height        BIGINT NOT NULL,
  previous_hash VARCHAR NOT NULL,
  merkle_hash   VARCHAR NOT NULL,
  timestamp     BIGINT  NOT NULL,
  signature     VARCHAR NOT NULL,
  type_id       INTEGER NOT NULL
);
--
CREATE TABLE main_blocks (
  id INTEGER PRIMARY KEY REFERENCES blocks
);
--
CREATE TABLE genesis_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  epoch_index BIGINT NOT NULL
);