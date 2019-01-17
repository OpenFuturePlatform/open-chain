CREATE TABLE states (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  address      VARCHAR NOT NULL,
  height_block BIGINT  NOT NULL,
  UNIQUE (address, height_block)
);

--
CREATE TABLE delegate_states (
  id       BIGINT PRIMARY KEY REFERENCES states,
  rating   BIGINT NOT NULL,
);

--
CREATE TABLE wallet_states (
  id      BIGINT PRIMARY KEY REFERENCES states,
  balance BIGINT NOT NULL,
);

--
CREATE TABLE wallet_states_votes (
  address   VARCHAR NOT NULL,
  node_id   VARCHAR NOT NULL REFERENCES delegates(node_id),
  PRIMARY KEY (address, node_id)
);
