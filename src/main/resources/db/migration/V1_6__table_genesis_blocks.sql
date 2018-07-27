CREATE TABLE genesis_blocks (
  id          INTEGER PRIMARY KEY REFERENCES blocks,
  epoch_index BIGINT NOT NULL
);

INSERT INTO genesis_blocks (id, epoch_index) VALUES (1, 1);