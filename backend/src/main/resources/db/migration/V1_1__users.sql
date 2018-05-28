CREATE TABLE users (
  id       BIGSERIAL PRIMARY KEY,
  email    VARCHAR NOT NULL UNIQUE,
  password VARCHAR NOT NULL,
  name     VARCHAR NOT NULL,
  age      INT     NOT NULL
);