CREATE TABLE blocks (
  id               INTEGER PRIMARY KEY,
  order_number     BIGINT  NOT NULL,
  timestamp        BIGINT  NOT NULL,
  merkle_hash      VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  previous_hash    VARCHAR NOT NULL,
  node_public_key VARCHAR NOT NULL,
  node_signature   VARCHAR NOT NULL
);