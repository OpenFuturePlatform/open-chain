CREATE TABLE transactions (
  id               INTEGER PRIMARY KEY,
  timestamp        BIGINT         NOT NULL,
  fee              BIGINT         NOT NULL,
  sender_address   VARCHAR        NOT NULL,
  hash             VARCHAR UNIQUE NOT NULL,
  sender_signature VARCHAR        NOT NULL,
  sender_key       VARCHAR        NOT NULL,
  block_id         INTEGER        NOT NULL REFERENCES main_blocks
);
--
CREATE TABLE transfer_transactions (
  id                INTEGER PRIMARY KEY REFERENCES transactions,
  amount            BIGINT  NOT NULL,
  recipient_address VARCHAR NOT NULL
);
--
CREATE TABLE delegate_transactions (
  id           INTEGER PRIMARY KEY REFERENCES transactions,
  delegate_key VARCHAR UNIQUE NOT NULL,
  delegate_host VARCHAR NOT NULL,
  delegate_port INTEGER NOT NULL
);
--
CREATE TABLE vote_types (
  id  INT PRIMARY KEY,
  key VARCHAR UNIQUE NOT NULL
);

INSERT INTO vote_types (id, key) VALUES (1, 'FOR');
INSERT INTO vote_types (id, key) VALUES (2, 'AGAINST');
--
CREATE TABLE vote_transactions (
  id           INTEGER PRIMARY KEY REFERENCES transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  delegate_key VARCHAR NOT NULL
);

-- UNCONFIRMED TABLES
CREATE TABLE u_transactions (
  id               INTEGER PRIMARY KEY,
  timestamp        BIGINT         NOT NULL,
  fee              BIGINT         NOT NULL,
  sender_address   VARCHAR        NOT NULL,
  hash             VARCHAR UNIQUE NOT NULL,
  sender_signature VARCHAR        NOT NULL,
  sender_key       VARCHAR        NOT NULL
);
--
CREATE TABLE u_transfer_transactions (
  id                INTEGER PRIMARY KEY REFERENCES u_transations,
  amount            BIGINT  NOT NULL,
  recipient_address VARCHAR NOT NULL
);
--
CREATE TABLE u_delegate_transactions (
  id            INTEGER PRIMARY KEY REFERENCES u_transactions,
  delegate_key  VARCHAR NOT NULL UNIQUE,
  delegate_host VARCHAR NOT NULL,
  delegate_port INTEGER NOT NULL
);
--
CREATE TABLE u_vote_transactions (
  id           INTEGER PRIMARY KEY REFERENCES u_transactions,
  vote_type_id INTEGER NOT NULL REFERENCES vote_types,
  delegate_key VARCHAR NOT NULL
);