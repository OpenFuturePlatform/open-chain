CREATE TABLE currencies (
  id INTEGER PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);
INSERT INTO currencies(id, key) VALUES (1, 'USD');
INSERT INTO currencies(id, key) VALUES (2, 'GBP');
INSERT INTO currencies(id, key) VALUES (3, 'EUR');
INSERT INTO currencies(id, key) VALUES (4, 'CNY');
INSERT INTO currencies(id, key) VALUES (5, 'ETH');
--
CREATE TABLE blocks (
  id  INTEGER PRIMARY KEY,
  version INTEGER NOT NULL,
  size INTEGER NOT NULL,
  timestamp BIGINT NOT NULL,
  merkle_hash VARCHAR NOT NULL,
  hash VARCHAR NOT NULL,
  previous_hash VARCHAR NOT NULL,
  signature VARCHAR NOT NULL
);
--
CREATE TABLE transactions (
  id  INTEGER PRIMARY KEY,
  block_id INTEGER NOT NULL REFERENCES blocks,
  hash VARCHAR NOT NULL,
  amount INTEGER NOT NULL,
  currency_id INTEGER  NOT NULL REFERENCES currencies,
  timestamp BIGINT NOT NULL,
  recipient_key VARCHAR NOT NULL,
  sender_key VARCHAR NOT NULL,
  signature VARCHAR NOT NULL
);