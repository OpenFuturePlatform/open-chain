CREATE TABLE temporary_blocks
(
  id     INTEGER PRIMARY KEY,
  height INTEGER UNIQUE NOT NULL,
  block  VARCHAR NOT NULL
);