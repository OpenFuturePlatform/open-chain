-- Roles
CREATE TABLE roles (
  id  BIGINT PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);

INSERT INTO roles (id, key) VALUES (1, 'ROLE_ADMIN');
INSERT INTO roles (id, key) VALUES (2, 'ROLE_USER');

-- Users to Roles relations
CREATE TABLE users2roles (
  user_id BIGINT REFERENCES users,
  role_id BIGINT REFERENCES roles,
  PRIMARY KEY (user_id, role_id)
);