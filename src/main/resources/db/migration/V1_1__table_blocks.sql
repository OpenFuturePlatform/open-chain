CREATE TABLE blocks (
  id            BIGINT PRIMARY KEY,
  hash          VARCHAR NOT NULL,
  height        BIGINT NOT NULL,
  previous_hash VARCHAR NOT NULL,
  merkle_hash   VARCHAR NOT NULL,
  timestamp     BIGINT  NOT NULL
);