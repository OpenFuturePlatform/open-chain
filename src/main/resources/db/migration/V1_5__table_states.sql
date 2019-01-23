CREATE TABLE states (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY HASH,
  address  VARCHAR NOT NULL,
  block_id BIGINT  NOT NULL REFERENCES blocks
);
--
CREATE UNIQUE HASH INDEX states_address_block_id
  ON states (address, block_id);
--
CREATE TABLE delegate_states (
  id     BIGINT PRIMARY KEY REFERENCES states,
  rating BIGINT NOT NULL,
);

--
CREATE TABLE wallet_states (
  id       BIGINT PRIMARY KEY REFERENCES states,
  balance  BIGINT NOT NULL,
  vote_for VARCHAR,
);
