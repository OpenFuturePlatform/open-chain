CREATE TABLE temporary_blocks
(
    id     serial PRIMARY KEY,
    height BIGINT UNIQUE NOT NULL,
    block  text NOT NULL
);