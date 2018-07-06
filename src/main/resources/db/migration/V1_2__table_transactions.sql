CREATE TABLE transactions (
  id            BIGINT PRIMARY KEY,
  block_id      BIGINT NOT NULL REFERENCES blocks,
  hash          VARCHAR NOT NULL,
  amount        INTEGER NOT NULL,
  timestamp     BIGINT  NOT NULL,
  recipient_key VARCHAR NOT NULL,
  sender_key    VARCHAR NOT NULL,
  signature     VARCHAR NOT NULL
);