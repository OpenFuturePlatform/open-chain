CREATE TABLE stakeholders (
  id         INTEGER PRIMARY KEY,
  username   VARCHAR NOT NULL,
  address    VARCHAR NOT NULL,
  public_key VARCHAR NOT NULL UNIQUE
);

CREATE TABLE delegates (
  id     INTEGER PRIMARY KEY REFERENCES stakeholders,
  rating INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE stakeholders_2_delegates (
  stakeholder_id BIGINT NOT NULL references stakeholders,
  delegate_id    BIGINT NOT NULL REFERENCES delegates,
  PRIMARY KEY (stakeholder_id, delegate_id)
);