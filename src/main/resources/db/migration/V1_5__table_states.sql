CREATE TABLE states (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  address  VARCHAR NOT NULL,
  block_id BIGINT  NOT NULL REFERENCES blocks,
  UNIQUE (address, block_id)
);

--
CREATE TABLE delegate_states (
  id     BIGINT PRIMARY KEY REFERENCES states,
  rating BIGINT NOT NULL,
);

--
CREATE TABLE wallet_states (
  id      BIGINT PRIMARY KEY REFERENCES states,
  balance BIGINT NOT NULL,
);

--
CREATE TABLE wallet_votes (
  address VARCHAR NOT NULL,
  node_id VARCHAR NOT NULL REFERENCES delegates(node_id),
  PRIMARY KEY (address, node_id)
);
