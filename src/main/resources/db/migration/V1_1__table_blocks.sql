CREATE TABLE blocks (
  id             INTEGER PRIMARY KEY,
  height         BIGINT NOT NULL UNIQUE,
  nonce          BIGINT NOT NULL,
  timestamp      BIGINT NOT NULL UNIQUE,
  merkle_hash    VARCHAR NOT NULL UNIQUE ,
  hash           VARCHAR NOT NULL UNIQUE,
  previous_hash  VARCHAR NOT NULL UNIQUE,
  node_key       VARCHAR NOT NULL,
  node_signature VARCHAR NOT NULL
);