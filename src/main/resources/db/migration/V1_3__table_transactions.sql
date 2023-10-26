CREATE TABLE transactions (
  id               INTEGER PRIMARY KEY,
  timestamp        INTEGER  NOT NULL,
  fee              INTEGER  NOT NULL,
  sender_address   VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  signature        VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL,
  block_id         INTEGER  NOT NULL REFERENCES main_blocks
);
--
CREATE UNIQUE INDEX transaction_hash
  ON transactions (hash);
--
CREATE INDEX transaction_sender_address
  ON transactions (sender_address);
--
CREATE TABLE transfer_transactions (
  id                INTEGER PRIMARY KEY REFERENCES transactions,
  amount            INTEGER  NOT NULL,
  recipient_address VARCHAR,
  data              VARCHAR
);
--
CREATE INDEX transfer_transaction_recipient_address
  ON transfer_transactions (recipient_address);
--
CREATE TABLE reward_transactions (
  id                INTEGER PRIMARY KEY REFERENCES transactions,
  reward            INTEGER  NOT NULL,
  recipient_address VARCHAR NOT NULL
);
--
CREATE INDEX reward_transactions_recipient_address
  ON reward_transactions (recipient_address);
--
CREATE TABLE delegate_transactions (
  id            INTEGER PRIMARY KEY REFERENCES transactions,
  delegate_key  VARCHAR NOT NULL,
  amount        INTEGER  NOT NULL
);
--
CREATE UNIQUE INDEX delegate_transactions_delegate_key
  ON delegate_transactions (delegate_key);
--
CREATE TABLE vote_types (
  id  INTEGER PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);

INSERT INTO vote_types (id, key)
VALUES (1, 'FOR'),
       (2, 'AGAINST');
--
CREATE TABLE vote_transactions (
  id           INTEGER PRIMARY KEY REFERENCES transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  delegate_key VARCHAR NOT NULL
);
--
CREATE INDEX vote_transactions_delegate_key
  ON vote_transactions (delegate_key);
--


-- UNCONFIRMED TABLES
CREATE TABLE u_transactions (
  id               INTEGER PRIMARY KEY,
  timestamp        INTEGER  NOT NULL,
  fee              INTEGER  NOT NULL,
  sender_address   VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  signature        VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL
);
--
CREATE UNIQUE INDEX u_transactions_hash
  ON u_transactions (hash);
--
CREATE INDEX u_transactions_sender_address
  ON u_transactions (sender_address);
--
CREATE INDEX u_transactions_fee
  ON u_transactions (fee);
--
CREATE TABLE u_transfer_transactions (
  id                INTEGER PRIMARY KEY REFERENCES u_transactions,
  amount            INTEGER  NOT NULL,
  recipient_address VARCHAR,
  data              VARCHAR
);
--
CREATE TABLE u_delegate_transactions (
  id            INTEGER PRIMARY KEY REFERENCES u_transactions,
  delegate_key  VARCHAR NOT NULL,
  amount        INTEGER  NOT NULL
);
--
CREATE UNIQUE INDEX u_delegate_transactions_delegate_key
  ON u_delegate_transactions (delegate_key);
--
CREATE TABLE u_vote_transactions (
  id           INTEGER PRIMARY KEY REFERENCES u_transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  delegate_key VARCHAR NOT NULL
);
--
CREATE INDEX u_vote_transactions_delegate_key
  ON u_vote_transactions (delegate_key);
--