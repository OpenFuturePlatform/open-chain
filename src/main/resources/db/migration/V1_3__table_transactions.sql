CREATE TABLE transactions (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  timestamp        BIGINT  NOT NULL,
  fee              BIGINT  NOT NULL,
  sender_address   VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL UNIQUE,
  sender_signature VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL,
  block_id         BIGINT  NOT NULL REFERENCES main_blocks
);
--
CREATE INDEX transaction_sender_address
  ON transactions (sender_address);
--
CREATE INDEX transaction_hash
  ON transactions (hash);
--
CREATE TABLE transfer_transactions (
  id                BIGINT PRIMARY KEY REFERENCES transactions,
  amount            BIGINT  NOT NULL,
  recipient_address VARCHAR NOT NULL
);
--
CREATE INDEX transfer_transaction_recipient_address
  ON transfer_transactions (recipient_address);
--
CREATE TABLE reward_transactions (
  id                BIGINT PRIMARY KEY REFERENCES transactions,
  reward            BIGINT  NOT NULL,
  recipient_address VARCHAR NOT NULL
);
--
CREATE INDEX reward_transactions_recipient_address
  ON reward_transactions (recipient_address);
--
CREATE TABLE delegate_transactions (
  id            BIGINT PRIMARY KEY REFERENCES transactions,
  node_id       VARCHAR NOT NULL UNIQUE,
  delegate_key  VARCHAR NOT NULL UNIQUE,
  delegate_host VARCHAR NOT NULL,
  delegate_port INTEGER NOT NULL,
  amount        BIGINT  NOT NULL
);
--
CREATE TABLE vote_types (
  id  INTEGER AUTO_INCREMENT PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);

INSERT INTO vote_types (id, key)
VALUES (1, 'FOR'),
       (2, 'AGAINST');
--
CREATE TABLE vote_transactions (
  id           BIGINT PRIMARY KEY REFERENCES transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  node_id      VARCHAR NOT NULL
);
CREATE INDEX vote_transactions_vote_type_id_node_id
  ON vote_transactions (vote_type_id, node_id);
--

-- UNCONFIRMED TABLES
CREATE MEMORY TABLE u_transactions (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  timestamp        BIGINT  NOT NULL,
  fee              BIGINT  NOT NULL,
  sender_address   VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL UNIQUE,
  sender_signature VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL
);
CREATE INDEX u_transactions_sender_address
  ON u_transactions (sender_address);
--
--
CREATE INDEX u_transactions_fee
  ON u_transactions (fee);
--
CREATE INDEX u_transactions_hash
  ON u_transactions (hash);
--
CREATE MEMORY TABLE u_transfer_transactions (
  id                BIGINT PRIMARY KEY REFERENCES u_transactions,
  amount            BIGINT  NOT NULL,
  recipient_address VARCHAR NOT NULL
);
--
CREATE MEMORY TABLE u_delegate_transactions (
  id            BIGINT PRIMARY KEY REFERENCES u_transactions,
  node_id       VARCHAR NOT NULL UNIQUE,
  delegate_key  VARCHAR NOT NULL UNIQUE,
  delegate_host VARCHAR NOT NULL,
  delegate_port INTEGER NOT NULL,
  amount        BIGINT  NOT NULL
);
--
CREATE MEMORY TABLE u_vote_transactions (
  id           BIGINT PRIMARY KEY REFERENCES u_transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  node_id      VARCHAR NOT NULL
);
--
CREATE INDEX u_vote_transactions_vote_type_id_node_id
  ON u_vote_transactions (vote_type_id, node_id);
--