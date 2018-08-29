DROP VIEW IF EXISTS delegates_view;
CREATE VIEW IF NOT EXISTS delegates_view AS
  select
    d.id                as id,
    d.public_key        as public_key,
    d.address           as address,
    d.host              as host,
    d.port              as port,
    d.registration_date as registration_date,
    ifnull(sum(w.balance), 0)      as rating,
    ifnull(count(d.id), 0)         as votes_count
  from delegates as d
    left join wallets2delegates as w2d on d.id = w2d.delegate_id
    left join wallets as w on w.id = w2d.wallet_id
  group by d.public_key;