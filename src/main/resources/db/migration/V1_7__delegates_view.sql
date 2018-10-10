DROP VIEW IF EXISTS delegates_view;
CREATE VIEW IF NOT EXISTS delegates_view AS
  SELECT d.id                               AS id,
         d.public_key                       AS public_key,
         d.node_id                          AS node_id,
         d.address                          AS address,
         d.host                             AS host,
         d.port                             AS port,
         d.registration_date                AS registration_date,
         ifnull(sum(w.balance), 0)          AS rating,
         ifnull(count(wv.node_id), 0)       AS votes_count
  FROM delegates AS d
    LEFT JOIN wallet_votes AS wv ON d.node_id = wv.node_id
    LEFT JOIN wallets AS w ON w.address = wv.address
  GROUP BY d.public_key
  ORDER BY rating DESC, registration_date DESC;