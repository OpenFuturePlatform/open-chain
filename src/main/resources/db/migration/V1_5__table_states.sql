CREATE TABLE states (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  address      VARCHAR NOT NULL,
  height_block BIGINT  NOT NULL,
  UNIQUE (address, height_block)
);

--
CREATE TABLE node_states (
  id   BIGINT PRIMARY KEY REFERENCES states,
  data VARCHAR NOT NULL,
);

--
CREATE TABLE wallet_states (
  id   BIGINT PRIMARY KEY REFERENCES states,
  data VARCHAR NOT NULL,
);