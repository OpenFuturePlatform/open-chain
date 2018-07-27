CREATE TABLE main_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  merkle_hash VARCHAR NOT NULL
);