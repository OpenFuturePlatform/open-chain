CREATE TABLE delegates (
  id         INTEGER PRIMARY KEY,
  username   VARCHAR NOT NULL,
  address    VARCHAR NOT NULL,
  public_key VARCHAR NOT NULL UNIQUE,
  rating INTEGER NOT NULL DEFAULT 0
);