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
         ifnull(count(w2d.delegate_id), 0)  AS votes_count
  FROM delegates AS d
         LEFT JOIN wallets2delegates AS w2d ON d.id = w2d.delegate_id
         LEFT JOIN wallets AS w ON w.id = w2d.wallet_id
  GROUP BY d.public_key
  ORDER BY rating DESC, registration_date DESC;