CREATE TABLE receipts (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaction_hash  varchar(512) NOT NULL,
  result            text NOT NULL,
  `hash`              text NOT NULL,
  block_id          BIGINT  NOT NULL REFERENCES main_blocks
);
--
CREATE UNIQUE INDEX receipts_transaction_hash
  ON receipts (transaction_hash);
--