CREATE TABLE transactions (
  id            INTEGER PRIMARY KEY,
  block_id      INTEGER NULLABLE REFERENCES blocks,
  hash          VARCHAR NOT NULL UNIQUE,
  amount        BIGINT NOT NULL,
  timestamp     BIGINT  NOT NULL,
  recipient_key VARCHAR NOT NULL,
  sender_key    VARCHAR NOT NULL,
  signature     VARCHAR NOT NULL
);