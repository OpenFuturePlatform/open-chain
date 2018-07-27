insert into wallets(id, address, balance)
select 1, "delegate_1", 100;

insert into wallets(id, address, balance)
select 2, "delegate_2", 100;

insert into wallets(id, address, balance)
select 3, "delegate_3", 100;
--
insert into delegates(id, public_key, address)
select 1, "public_key_1", "delegate_1";

insert into delegates(id, public_key, address)
select 2, "public_key_2", "delegate_2";

insert into delegates(id, public_key, address)
select 3, "public_key_3", "delegate_3";
--
insert into wallets2delegates(wallet_id, delegate_id)
select 1, 1;

insert into wallets2delegates(wallet_id, delegate_id)
select 2, 2;

insert into wallets2delegates(wallet_id, delegate_id)
select 3, 3;
--
insert into delegate2genesis(delegate_id, genesis_id)
select 1, 1;

insert into delegate2genesis(delegate_id, genesis_id)
select 2, 1;

insert into delegate2genesis(delegate_id, genesis_id)
select 3, 1;