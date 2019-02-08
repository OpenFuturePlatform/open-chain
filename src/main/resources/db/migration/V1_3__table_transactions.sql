CREATE TABLE transactions (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY HASH,
  timestamp        BIGINT  NOT NULL,
  fee              BIGINT  NOT NULL,
  sender_address   VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  signature        VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL,
  block_id         BIGINT  NOT NULL REFERENCES main_blocks
);
--
CREATE UNIQUE HASH INDEX transaction_hash
  ON transactions (hash);
--
CREATE HASH INDEX transaction_sender_address
  ON transactions (sender_address);
--
CREATE TABLE transfer_transactions (
  id                BIGINT PRIMARY KEY REFERENCES transactions,
  amount            BIGINT  NOT NULL,
  recipient_address VARCHAR,
  data              VARCHAR
);
--
CREATE HASH INDEX transfer_transaction_recipient_address
  ON transfer_transactions (recipient_address);
--
CREATE TABLE reward_transactions (
  id                BIGINT PRIMARY KEY REFERENCES transactions,
  reward            BIGINT  NOT NULL,
  recipient_address VARCHAR NOT NULL
);
--
CREATE HASH INDEX reward_transactions_recipient_address
  ON reward_transactions (recipient_address);
--
CREATE TABLE delegate_transactions (
  id            BIGINT PRIMARY KEY REFERENCES transactions,
  delegate_key  VARCHAR NOT NULL,
  amount        BIGINT  NOT NULL
);
--
CREATE UNIQUE HASH INDEX delegate_transactions_delegate_key
  ON delegate_transactions (delegate_key);
--
CREATE TABLE vote_types (
  id  INTEGER AUTO_INCREMENT PRIMARY KEY HASH,
  key VARCHAR NOT NULL UNIQUE
);

INSERT INTO vote_types (id, key)
VALUES (1, 'FOR'),
       (2, 'AGAINST');
--
CREATE TABLE vote_transactions (
  id           BIGINT PRIMARY KEY REFERENCES transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  delegate_key VARCHAR NOT NULL
);
--
CREATE HASH INDEX vote_transactions_delegate_key
  ON vote_transactions (delegate_key);
--


-- UNCONFIRMED TABLES
CREATE MEMORY TABLE u_transactions (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY HASH,
  timestamp        BIGINT  NOT NULL,
  fee              BIGINT  NOT NULL,
  sender_address   VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  signature        VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL
);
--
CREATE UNIQUE HASH INDEX u_transactions_hash
  ON u_transactions (hash);
--
CREATE HASH INDEX u_transactions_sender_address
  ON u_transactions (sender_address);
--
CREATE INDEX u_transactions_fee
  ON u_transactions (fee);
--
CREATE MEMORY TABLE u_transfer_transactions (
  id                BIGINT PRIMARY KEY REFERENCES u_transactions,
  amount            BIGINT  NOT NULL,
  recipient_address VARCHAR,
  data              VARCHAR,
);
--
CREATE MEMORY TABLE u_delegate_transactions (
  id            BIGINT PRIMARY KEY REFERENCES u_transactions,
  delegate_key  VARCHAR NOT NULL,
  amount        BIGINT  NOT NULL
);
--
CREATE UNIQUE HASH INDEX u_delegate_transactions_delegate_key
  ON u_delegate_transactions (delegate_key);
--
CREATE MEMORY TABLE u_vote_transactions (
  id           BIGINT PRIMARY KEY REFERENCES u_transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  delegate_key VARCHAR NOT NULL
);
--
CREATE INDEX u_vote_transactions_delegate_key
  ON u_vote_transactions (delegate_key);
--