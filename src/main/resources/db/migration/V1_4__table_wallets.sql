CREATE TABLE wallets (
  id            INTEGER PRIMARY KEY,
  address       VARCHAR NOT NULL,
  balance       DOUBLE
);

ALTER TABLE transactions ADD COLUMN sender_address VARCHAR;
ALTER TABLE transactions ADD COLUMN recipient_address VARCHAR;
