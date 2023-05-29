CREATE TABLE temporary_blocks
(
  id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  height BIGINT UNIQUE NOT NULL,
  block  text NOT NULL
);