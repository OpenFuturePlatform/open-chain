CREATE TABLE receipts (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY HASH,
  transaction_hash  VARCHAR NOT NULL,
  result            VARCHAR NOT NULL,
  hash              VARCHAR NOT NULL,
  block_id          BIGINT  NOT NULL REFERENCES main_blocks
);
--
CREATE UNIQUE HASH INDEX receipts_transaction_hash
  ON receipts (transaction_hash);
--