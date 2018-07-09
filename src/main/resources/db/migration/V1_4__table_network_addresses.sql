CREATE TABLE network_addresses (
  id         INTEGER PRIMARY KEY,
  node_id VARCHAR NOT NULL UNIQUE,
  host       VARCHAR NOT NULL,
  port       INTEGER NOT NULL
);