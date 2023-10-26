CREATE TABLE receipts (
  id                INTEGER PRIMARY KEY,
  transaction_hash  VARCHAR NOT NULL,
  result            VARCHAR NOT NULL,
  hash              VARCHAR NOT NULL,
  block_id          INTEGER  NOT NULL REFERENCES main_blocks
);
--
CREATE UNIQUE INDEX receipts_transaction_hash
  ON receipts (transaction_hash);
--