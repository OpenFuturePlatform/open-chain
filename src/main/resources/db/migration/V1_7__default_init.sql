INSERT INTO blocks (
id,
timestamp,
height,
previous_hash,
reward,
hash,
signature,
public_key
) VALUES (
         1,
         1532345018021,
         1,
         '',
         0,
         '838c84179c7e644cdf2ff0af3055ed45c6f43e0bd7634f8bd6ae7d088b1aaf0a',
         'MEUCIQCLeQuqCrDd8nmS037ZfmQNtpUf/AsfQilmK7CcNNIi7QIgKNdhszih/PezHW52v4/tdsZxaLovJzDnLvUy98tnsgg=',
         '038bbbeeb867b999991cd5b146b392ba2fe44ea69d1cc7208e32190184b13aaf1b'
         );

INSERT INTO genesis_blocks (id, epoch_index) VALUES (1, 1);

insert into wallets (id, address, balance)
values (1, '0x82d80f07F137322a4BFbfB109DEeecd46c7cB45C', 0),
       (2, '0x54017283B19df0Da5A4d8f24f9a06CeF80f52CB0', 0),
       (3, '0xB5F62e041d2Ddd03440EF5AC17c78FcE401c7594', 0),
       (4, '0x42c2e3Ef3329CBA0E69d7B95dcE23A5275369823', 0),
       (5, '0x431320643cac62822F109128265369e6E379DBb4', 0),
       (6, '0x52e4A597cD484d4908899d84a48EFfB7cf92a335', 0),
       (7, '0x6b19b2dEE50f8D8b6c903b3A369dF00291e3c405', 1000);
--
insert into delegates (id, public_key, address)
values (1, '0202ea9f8a2a11e17b36911f0a143e13b289531f0a3fc79124ef596659c7a68ff3', '0x82d80f07F137322a4BFbfB109DEeecd46c7cB45C'),
       (2, '0278172a4763f9b8f73ee4ae0d24c0020fd7b151db5b1022760ec21802ff61fbff', '0x54017283B19df0Da5A4d8f24f9a06CeF80f52CB0'),
       (3, '0388060d4242732340c7f626a2527361ef168a530ba8f6b2c279b8c3cffd59a0f4', '0xB5F62e041d2Ddd03440EF5AC17c78FcE401c7594'),
       (4, '032bddcdefcebf73ad121a663d657e96ace8f412c900b1a79d5c502b7cfc2ecba8', '0x42c2e3Ef3329CBA0E69d7B95dcE23A5275369823'),
       (5, '03bd2e25db207c727c63f568d3d00759265e333b45ebaaf68777ede03594155191', '0x431320643cac62822F109128265369e6E379DBb4'),
       (6, '02c1cccd2867ef089d1c0a291a30ec85cdd95655291c2f4aef10dfdb39cb35cb22', '0x52e4A597cD484d4908899d84a48EFfB7cf92a335');
--
insert into wallets2delegates (wallet_id, delegate_id)
values (1, 1),
       (2, 2),
       (3, 3),
       (4, 4),
       (5, 5),
       (6, 6);
--
insert into delegate2genesis (delegate_id, genesis_id)
values (1, 1),
       (2, 1),
       (3, 1),
       (4, 1),
       (5, 1),
       (6, 1);