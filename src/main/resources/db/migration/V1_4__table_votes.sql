CREATE TABLE votes (
  id             INTEGER PRIMARY KEY,
  transaction_id INTEGER NOT NULL REFERENCES transactions,
  public_key     VARCHAR NOT NULL,
  weight         INTEGER NOT NULL
);
