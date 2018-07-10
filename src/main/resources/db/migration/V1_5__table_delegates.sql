CREATE TABLE accounts (
  id         INTEGER PRIMARY KEY,
  username   VARCHAR NOT NULL,
  address    VARCHAR NOT NULL,
  public_key VARCHAR NOT NULL UNIQUE
);

CREATE TABLE delegates (
  id         INTEGER PRIMARY KEY REFERENCES accounts,
  rating INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE accounts_2_delegates(
  account_id BIGINT NOT NULL references accounts,
  delegate_id BIGINT NOT NULL REFERENCES delegates,
  PRIMARY KEY (account_id, delegate_id)
);