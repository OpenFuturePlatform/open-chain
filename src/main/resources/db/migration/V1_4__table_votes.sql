CREATE TABLE transactions (
  id             INTEGER PRIMARY KEY,
  transaction_id INTEGER NOT NULL REFERENCES transactions,
  public_key     VARCHAR NOT NULL,
  wieght         INTEGER NOT NULL
);
