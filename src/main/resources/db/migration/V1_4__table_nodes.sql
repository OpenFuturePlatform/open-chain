CREATE TABLE nodes (
  public_key BLOB PRIMARY KEY,
  host VARCHAR NOT NULL,
  port INTEGER NOT NULL
);