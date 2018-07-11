CREATE TABLE transactions (
  id                INTEGER PRIMARY KEY,
  timestamp         BIGINT  NOT NULL,
  amount            BIGINT  NOT NULL,
  recipient_key     VARCHAR NOT NULL,
  recipient_address VARCHAR NOT NULL,
  sender_key        VARCHAR NOT NULL,
  sender_address    VARCHAR NOT NULL,
  sender_signature  VARCHAR NOT NULL,
  hash              VARCHAR NOT NULL,
  block_id          INTEGER NULLABLE REFERENCES blocks
);
--
CREATE TABLE transfer_transactions (
  id INTEGER PRIMARY KEY REFERENCES transactions
);
--
CREATE TABLE vote_types (
  id  INT PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);
INSERT INTO vote_types (id, key) VALUES (1, 'FOR');
INSERT INTO vote_types (id, key) VALUES (2, 'AGAINST');
--
CREATE TABLE vote_transactions (
  id           INTEGER PRIMARY KEY REFERENCES transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  delegate_key VARCHAR NOT NULL,
  weight       INTEGER NOT NULL
);
