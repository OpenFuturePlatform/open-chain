insert into wallets(id, address, balance)
select 1, 'Test wallet address', 100;
--
insert into delegates(id, public_key, address)
select 1, 'Test delegate public key', 'Test delegate address';
--
insert into wallets2delegates(wallet_id, delegate_id)
select 1, 1;
--
insert into delegate2genesis(delegate_id, genesis_id)
select 1, 1;