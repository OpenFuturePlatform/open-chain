CREATE TABLE contracts (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  address  varchar(512) NOT NULL,
  owner    text NOT NULL,
  bytecode text NOT NULL,
  abi text NOT NULL,
  cost BIGINT NOT NULL
);
--
CREATE UNIQUE INDEX contracts_address
  ON contracts (address);
--