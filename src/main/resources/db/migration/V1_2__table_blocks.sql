CREATE TABLE blocks (
  id            INTEGER PRIMARY KEY,
  hash          VARCHAR UNIQUE NOT NULL,
  height        BIGINT  NOT NULL,
  previous_hash VARCHAR NOT NULL,
  timestamp     BIGINT  NOT NULL,
  signature     VARCHAR NOT NULL,
  public_key    VARCHAR NOT NULL
);
--
CREATE TABLE main_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  merkle_hash VARCHAR NOT NULL
);
--
CREATE TABLE genesis_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  epoch_index BIGINT  NOT NULL
);