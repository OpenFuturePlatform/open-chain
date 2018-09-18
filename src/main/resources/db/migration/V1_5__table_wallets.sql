CREATE TABLE wallets (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  address            VARCHAR NOT NULL UNIQUE,
  balance            BIGINT DEFAULT 0,
  unconfirmed_output BIGINT DEFAULT 0
);

CREATE TABLE wallets2delegates (
  wallet_id   BIGINT NOT NULL REFERENCES wallets,
  delegate_id BIGINT NOT NULL REFERENCES delegates,
  PRIMARY KEY (wallet_id, delegate_id)
);