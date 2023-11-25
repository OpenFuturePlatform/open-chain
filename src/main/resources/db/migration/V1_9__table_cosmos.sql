CREATE TABLE tendermint_transactions (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY HASH,
  timestamp        BIGINT  NOT NULL,
  fee              BIGINT  NOT NULL,
  sender_address   VARCHAR NOT NULL,
  hash             VARCHAR NOT NULL,
  signature        VARCHAR NOT NULL,
  sender_key       VARCHAR NOT NULL
);
--
CREATE UNIQUE HASH INDEX tendermint_transactions_hash
  ON tendermint_transactions (hash);
--
CREATE HASH INDEX tendermint_transactions_sender_address
  ON tendermint_transactions (sender_address);
--
CREATE INDEX tendermint_transactions_fee
    ON tendermint_transactions (fee);
--
CREATE TABLE tendermint_transfer_transactions (
  id                BIGINT PRIMARY KEY REFERENCES tendermint_transactions,
  amount            BIGINT  NOT NULL,
  recipient_address VARCHAR,
  data              VARCHAR
);
--