CREATE TABLE delegates (
  id         INTEGER PRIMARY KEY,
  username   VARCHAR NOT NULL,
  address    VARCHAR NOT NULL,
  public_key VARCHAR NOT NULL UNIQUE,
  rating INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE delegates_votes(
  vote_sender_id BIGINT REFERENCES delegates,
  vote_recipient_id BIGINT NOT NULL REFERENCES delegates,
  PRIMARY KEY (vote_sender_id, vote_recipient_id)
);