CREATE TABLE wallets (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  address            VARCHAR NOT NULL UNIQUE,
  balance            BIGINT DEFAULT 0,
  unconfirmed_output BIGINT DEFAULT 0
);

CREATE TABLE wallet_votes (
  address   VARCHAR NOT NULL REFERENCES wallets(address),
  node_id   VARCHAR NOT NULL REFERENCES delegates(node_id),
  PRIMARY KEY (address, node_id)
);