CREATE TABLE states (
  id       INTEGER PRIMARY KEY,
  address  VARCHAR NOT NULL,
  hash     VARCHAR NOT NULL
);
--
CREATE UNIQUE INDEX states_address
  ON states (address);
--
CREATE TABLE account_states (
  id       INTEGER PRIMARY KEY REFERENCES states,
  balance  INTEGER NOT NULL,
  vote_for VARCHAR,
  storage  VARCHAR
);
--
CREATE TABLE delegate_states (
  id             INTEGER PRIMARY KEY REFERENCES states,
  rating         INTEGER  NOT NULL,
  wallet_address VARCHAR NOT NULL,
  create_date    INTEGER  NOT NULL
);