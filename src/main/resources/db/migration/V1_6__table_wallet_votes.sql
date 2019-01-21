CREATE TABLE wallet_votes (
  address VARCHAR NOT NULL,
  node_id VARCHAR NOT NULL REFERENCES delegates(node_id),
  PRIMARY KEY (address, node_id)
);
