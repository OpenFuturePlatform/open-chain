CREATE TABLE blocks (
  id            INTEGER PRIMARY KEY,
  timestamp     BIGINT         NOT NULL,
  height        BIGINT         NOT NULL,
  hash          VARCHAR UNIQUE NOT NULL,
  signature     VARCHAR        NOT NULL,
  public_key    VARCHAR        NOT NULL
);
--
CREATE TABLE main_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  previous_hash VARCHAR        NOT NULL,
  reward        BIGINT         NOT NULL DEFAULT 0,
  merkle_hash VARCHAR NOT NULL
);
--
CREATE TABLE genesis_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  previous_hash VARCHAR        NOT NULL,
  reward        BIGINT         NOT NULL DEFAULT 0,
  epoch_index BIGINT NOT NULL
);