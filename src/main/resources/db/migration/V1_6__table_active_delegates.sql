CREATE TABLE active_delegates (
  delegate_key      VARCHAR NOT NULL,
  genesis_block_key BIGINT NOT NULL REFERENCES genesis_blocks
);