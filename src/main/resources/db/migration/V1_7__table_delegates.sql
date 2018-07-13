CREATE TABLE delegates (
  id     INTEGER PRIMARY KEY,
  host   VARCHAR NOT NULL,
  port   INTEGER NOT NULL,
  rating INTEGER NOT NULL DEFAULT 0,
  UNIQUE (host, port)
);

CREATE TABLE delegate2genesis (
  delegate_id INTEGER NOT NULL REFERENCES delegates,
  genesis_id INTEGER NOT NULL REFERENCES genesis_blocks,
  PRIMARY KEY (delegate_id, genesis_id)
);