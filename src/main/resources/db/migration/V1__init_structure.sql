CREATE TABLE blocks (
  id  INTEGER PRIMARY KEY,
  order_number INTEGER NOT NULL,
  nonce BIGINT NOT NULL,
  timestamp BIGINT NOT NULL,
  merkle_hash VARCHAR NOT NULL,
  hash VARCHAR NOT NULL,
  previous_hash VARCHAR NOT NULL,
  node_key VARCHAR NOT NULL,
  node_signature VARCHAR NOT NULL
);

--
CREATE TABLE transactions (
  id  INTEGER PRIMARY KEY,
  block_id INTEGER NOT NULL REFERENCES blocks,
  hash VARCHAR NOT NULL,
  amount INTEGER NOT NULL,
  timestamp BIGINT NOT NULL,
  recipient_key VARCHAR NOT NULL,
  sender_key VARCHAR NOT NULL,
  signature VARCHAR NOT NULL
);