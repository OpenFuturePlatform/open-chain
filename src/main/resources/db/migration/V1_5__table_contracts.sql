CREATE TABLE contracts (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  address  VARCHAR NOT NULL,
  owner    VARCHAR NOT NULL,
  bytecode VARCHAR NOT NULL,
  abi VARCHAR NOT NULL,
  cost BIGINT NOT NULL
);
--
CREATE UNIQUE INDEX contracts_address
  ON contracts (address);
--