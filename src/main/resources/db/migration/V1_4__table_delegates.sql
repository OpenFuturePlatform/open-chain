CREATE TABLE delegates (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  public_key        VARCHAR NOT NULL UNIQUE,
  node_id           VARCHAR NOT NULL UNIQUE,
  address           VARCHAR NOT NULL,
  host              VARCHAR NOT NULL,
  port              INTEGER NOT NULL,
  registration_date BIGINT  NOT NULL
);
--
CREATE INDEX delegates_public_key
  ON delegates (public_key);
--
CREATE INDEX delegates_node_id
  ON delegates (node_id);
--
CREATE TABLE delegate2genesis (
  delegate_id BIGINT NOT NULL REFERENCES delegates,
  genesis_id  BIGINT NOT NULL REFERENCES genesis_blocks,
  PRIMARY KEY (delegate_id, genesis_id)
);