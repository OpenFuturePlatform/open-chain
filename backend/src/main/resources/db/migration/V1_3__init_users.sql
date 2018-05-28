INSERT INTO users (email, password, name, age)
VALUES
  ('customer', '$2a$10$5zTg.iwZ42mNEfKiSqRn7uw6ti5UVZW6P/eaGC/fkop2BRCSUr1s6' /*customer*/, 'Jone', 18),
  ('jone@example.io', '$2a$10$5zTg.iwZ42mNEfKiSqRn7uw6ti5UVZW6P/eaGC/fkop2BRCSUr1s6' /*customer*/, 'Jone', 18),
  ('karl@example.io', '$2a$10$5zTg.iwZ42mNEfKiSqRn7uw6ti5UVZW6P/eaGC/fkop2BRCSUr1s6', 'Karl', 23),
  ('smite@example.io', '$2a$10$5zTg.iwZ42mNEfKiSqRn7uw6ti5UVZW6P/eaGC/fkop2BRCSUr1s6', 'Smite', 46),
  ('sara@example.io', '$2a$10$5zTg.iwZ42mNEfKiSqRn7uw6ti5UVZW6P/eaGC/fkop2BRCSUr1s6', 'Sara', 35),
  ('pole@example.io', '$2a$10$5zTg.iwZ42mNEfKiSqRn7uw6ti5UVZW6P/eaGC/fkop2BRCSUr1s6', 'Pole', 39);

INSERT INTO users2roles (user_id, role_id)
VALUES (1, 1), (2, 1), (3, 1), (4, 2), (5, 2);
