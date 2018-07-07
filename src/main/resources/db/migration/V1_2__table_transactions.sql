CREATE TABLE transaction_types (
  id  INT PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);
INSERT INTO transaction_types (id, key) VALUES (1, 'VOTE');
INSERT INTO transaction_types (id, key) VALUES (2, 'DELEGATE_REGISTRATION');

--
CREATE TABLE transactions (
  id               INTEGER PRIMARY KEY,
  type_id          INTEGER NOT NULL REFERENCES transaction_types,
  timestamp        BIGINT  NOT NULL,
  amount           BIGINT  NOT NULL,
  recipient_key    VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL,
  sender_signature VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  block_id         INTEGER NULLABLE REFERENCES blocks
);

CREATE TABLE vote_transactions (
  id INTEGER PRIMARY KEY REFERENCES transactions
);

--
CREATE TABLE votes (
  id             INTEGER PRIMARY KEY,
  transaction_id INTEGER NOT NULL REFERENCES vote_transactions,
  public_key     VARCHAR NOT NULL,
  weight         INTEGER NOT NULL
)