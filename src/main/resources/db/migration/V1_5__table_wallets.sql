CREATE TABLE wallets (
  id                 INTEGER PRIMARY KEY,
  address            VARCHAR UNIQUE NOT NULL,
  balance            BIGINT DEFAULT 0,
  unconfirmed_output BIGINT DEFAULT 0
);

CREATE TABLE wallets2delegates (
  wallet_id   BIGINT NOT NULL REFERENCES wallets,
  delegate_id BIGINT NOT NULL REFERENCES delegates,
  PRIMARY KEY (wallet_id, delegate_id)
);