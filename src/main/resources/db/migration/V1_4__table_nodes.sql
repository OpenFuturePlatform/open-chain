CREATE TABLE nodes (
  id         INTEGER PRIMARY KEY,
  network_id VARCHAR NOT NULL UNIQUE,
  host       VARCHAR NOT NULL,
  port       INTEGER NOT NULL
);