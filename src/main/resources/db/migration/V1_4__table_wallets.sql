CREATE TABLE wallets (
  id            INTEGER PRIMARY KEY,
  address       VARCHAR NOT NULL,
  balance       DOUBLE
);

ALTER TABLE transactions ADD COLUMN 'from' VARCHAR;
ALTER TABLE transactions ADD COLUMN 'to' VARCHAR;
