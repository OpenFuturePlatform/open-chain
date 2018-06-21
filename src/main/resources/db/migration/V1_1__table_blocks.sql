CREATE TABLE blocks (
  id             INTEGER PRIMARY KEY,
  order_number   INTEGER NOT NULL,
  nonce          BIGINT  NOT NULL,
  timestamp      BIGINT  NOT NULL,
  merkle_hash    VARCHAR NOT NULL,
  hash           VARCHAR NOT NULL,
  previous_hash  VARCHAR NOT NULL,
  node_key       VARCHAR NOT NULL,
  node_signature VARCHAR NOT NULL
);