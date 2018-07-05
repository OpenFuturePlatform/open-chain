CREATE TABLE transaction_types (
  id  INT PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);

CREATE TABLE transactions (
  id              INTEGER PRIMARY KEY,
  block_id        INTEGER NULLABLE REFERENCES blocks,
  type_id         INTEGER NOT NULL REFERENCES transaction_types,
  timestamp       BIGINT  NOT NULL,
  hash            VARCHAR NOT NULL
);

CREATE TABLE vote_transactions (
  id INTEGER PRIMARY KEY REFERENCES transactions
);

CREATE TABLE transfer_transactions (
  id            INTEGER PRIMARY KEY REFERENCES transactions,
  amount        INTEGER NOT NULL,
  recipient_key VARCHAR NOT NULL,
  sender_key    VARCHAR NOT NULL
);