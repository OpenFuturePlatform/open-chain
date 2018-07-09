CREATE TABLE accounts (
  id         INTEGER PRIMARY KEY,
  username   VARCHAR NOT NULL,
  address    VARCHAR NOT NULL,
  public_key VARCHAR NOT NULL UNIQUE,
  rating INTEGER NOT NULL DEFAULT 0,
  is_delegate BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE accounts_2_delegates(
  account_id BIGINT,
  delegate_id BIGINT NOT NULL REFERENCES accounts,
  PRIMARY KEY (account_id, delegate_id)
);