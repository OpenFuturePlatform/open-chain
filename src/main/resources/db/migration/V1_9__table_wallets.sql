CREATE TABLE wallets (
  id            INTEGER PRIMARY KEY,
  address       VARCHAR NOT NULL UNIQUE,
  balance       BIGINT
);