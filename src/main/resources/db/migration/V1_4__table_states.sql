CREATE TABLE states (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  address  VARCHAR NOT NULL,
  hash     VARCHAR NOT NULL
);
--
CREATE UNIQUE INDEX states_address
  ON states (address);
--
CREATE TABLE account_states (
  id       BIGINT PRIMARY KEY REFERENCES states,
  balance  BIGINT NOT NULL,
  vote_for VARCHAR,
  storage  VARCHAR
);
--
CREATE TABLE delegate_states (
  id             BIGINT PRIMARY KEY REFERENCES states,
  rating         BIGINT  NOT NULL,
  wallet_address VARCHAR NOT NULL,
  create_date    BIGINT  NOT NULL
);