CREATE TABLE states (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  address  varchar(512) NOT NULL,
  hash     varchar(512) NOT NULL
);
--
CREATE UNIQUE INDEX states_address
  ON states (address);
--
CREATE TABLE account_states (
  id       BIGINT PRIMARY KEY REFERENCES states,
  balance  BIGINT NOT NULL,
  vote_for text,
  storage  text
);
--
CREATE TABLE delegate_states (
  id             BIGINT PRIMARY KEY REFERENCES states,
  rating         BIGINT  NOT NULL,
  wallet_address text NOT NULL,
  create_date    BIGINT  NOT NULL
);