CREATE TABLE wallet_votes (
  address VARCHAR NOT NULL,
  delegate_key VARCHAR NOT NULL REFERENCES delegates(node_id),
  PRIMARY KEY (address, delegate_key)
);
