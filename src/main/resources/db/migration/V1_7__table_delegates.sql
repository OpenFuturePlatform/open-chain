CREATE TABLE delegates (
  id     INTEGER PRIMARY KEY,
  public_key VARCHAR NULL UNIQUE,
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

INSERT INTO delegates (
  id,
  public_key,
  host,
  port,
  rating
) VALUES (
  1,
  '038bbbeeb867b999991cd5b146b392ba2fe44ea69d1cc7208e32190184b13aaf1b',
  'localhost',
  9190,
  1000000000
);

INSERT INTO delegate2genesis (delegate_id, genesis_id) VALUES (1, 1);