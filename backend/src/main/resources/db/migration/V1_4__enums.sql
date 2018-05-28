-- Issue Statuses Dictionary
CREATE TABLE issue_statuses (
  id INT PRIMARY KEY,
  key VARCHAR NOT NULL UNIQUE
);

INSERT INTO issue_statuses (id, key) VALUES (1, 'TO_DO');
INSERT INTO issue_statuses (id, key) VALUES (2, 'IN_PROGRESS');
INSERT INTO issue_statuses (id, key) VALUES (3, 'TESTING');
INSERT INTO issue_statuses (id, key) VALUES (4, 'DISPUTED');
INSERT INTO issue_statuses (id, key) VALUES (5, 'READY_FOR_REVIEW');
INSERT INTO issue_statuses (id, key) VALUES (6, 'ACCEPTED');