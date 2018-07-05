CREATE TABLE blocks (
  id             INTEGER PRIMARY KEY,
  height         BIGINT NOT NULL,
  nonce          BIGINT  NOT NULL,
  timestamp      BIGINT  NOT NULL,
  merkle_hash    VARCHAR NOT NULL,
  hash           VARCHAR NOT NULL,
  previous_hash  VARCHAR NOT NULL,
  node_key       VARCHAR NOT NULL,
  signature VARCHAR NOT NULL
);