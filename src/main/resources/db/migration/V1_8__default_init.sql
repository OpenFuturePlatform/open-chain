INSERT INTO blocks (id, TIMESTAMP, height, previous_hash, hash, signature, public_key)
VALUES (1,
        1532345018021,
        1,
        '',
        '551c9603385edbdd7917e748d65f7ee1496280f7f491c56a6c907743e68188bb',
        'MEUCIQD14fppWIIrdNtljuDEpSA33loW5GcPB9Q938A+pQJ31wIgRHq0vE6ogzZvyr62wIxsO42UPKHJxy8C9RIpZnYN3cA=',
        '02b04aa1832e799503000a6b8da1cdbb737c167fc829472c726a12ad9a4ccf24eb');

INSERT INTO genesis_blocks (id, epoch_index)
VALUES (1, 1);

INSERT INTO delegate2genesis (public_key, genesis_id)
VALUES ('02b04aa1832e799503000a6b8da1cdbb737c167fc829472c726a12ad9a4ccf24eb', 1),
       ('02c6847fcdc0239581151d1b05e7004c331eba097ae381349220c7cb0c5f5df9b3', 1),
       ('02c4aedc4a7e2d8cc0e73e6dfb428e8555adc8a1b74207cb61143babd3e04be63f', 1),
       ('02203492b48445da0f7392f6fa88d902f71d1b3714740ed99f43009a70fd7f6da8', 1),
       ('029a9b6a44d2e322af6884a00660d63ab80effceb0a80f86bd7b21fbf5ee1550ac', 1),
       ('020c08e5367fd881e52af43532db814d371b6bd3effb14442ad1044e71c0c0e41a', 1),
       ('02aef406b4c4a3c007094a05c2d2a2d815133a41914c96385a2d9ca71529b4d302', 1),
       ('03bfcc7afddf4f00c043faca2254ca8f09e3109c20b830d44a9b4438b363b9865e', 1),
       ('03b49d9a127c271fad4bcdf88bd9fb3430b122044972654dfe78a754c5e3064f4f', 1),
       ('03679e387bae8b7b724edc42a8149b7aa426edfc9ad54a1fc5e717ab081aca4daf', 1),
       ('036a1a1a6e952083beb1eb5213168288592cd000b42502bd4b8b1e74a465a2eacc', 1),
       ('029137a16dcea3967e8fd46dff0d812a2e60a57bef3eb6a7007867c0496631c5d6', 1),
       ('03a9623189c1da22cec1338d2ab0a982e51794aefb45107d7c4c000a09fc772204', 1),
       ('0283d909d2a886e9274f76f0460625e72674222b6a2bc937071858aa76a6e08d78', 1),
       ('02f8f3aca6fbf37e7dfd4cf55cf6a1dcffa2cc6cb0c2e513f8121dfb4d861bf04e', 1),
       ('039745d56241820f2a385c77aca013ecdff0b9fdce01d3f45ed34752cc9aa62cda', 1),
       ('02e7cb6589255a6e153d181c19aa8a34c5c0e6cef0c0374e0c8ba4b5f36ccfc18a', 1),
       ('02c2aca26e916926fce4101f0633009ae1c8c97e3081b3779880f6683ea258599c', 1),
       ('027d26f614afe8b6b3c8efb861c6666985701d76efa70c9d5a02f44c1e0be804ab', 1),
       ('0225aebdbb8ea2d8c401a638c87da670d5e2f0e4fdb9197f09ae75b2c805046724', 1),
       ('02e20add31fbf82b1369e8f2f537df9225a05d6fd497e2f9c65ee9b2df176c01c8', 1);

INSERT INTO states (id, address, hash, block_id)
VALUES (1, '0x0000000000000000000000000000000000000000',
        'f4639eccbed9ef1b911aed167b38ced71f525ff0b92e44d797a3ef6e856b788c', 1),
       (2, '02b04aa1832e799503000a6b8da1cdbb737c167fc829472c726a12ad9a4ccf24eb',
        '07e4bcc3aa017a3cfc370fa26c7bdc735c49a7f0000489260caadb077289bb38', 1),
       (3, '02c6847fcdc0239581151d1b05e7004c331eba097ae381349220c7cb0c5f5df9b3',
        'a98111345037c3bb230351dc6ae9c2c69809497b69f3c1227ace6a2b344063da', 1),
       (4, '02c4aedc4a7e2d8cc0e73e6dfb428e8555adc8a1b74207cb61143babd3e04be63f',
        '5929237c69bc41d05aae61d0e303fe5d71595d57b0fa70ec69a1707cbbf86202', 1),
       (5, '02203492b48445da0f7392f6fa88d902f71d1b3714740ed99f43009a70fd7f6da8',
        '80b7b98232cafb3f04cf09280f3be1fc48c3e59d11f97485ec895857538a0a75', 1),
       (6, '029a9b6a44d2e322af6884a00660d63ab80effceb0a80f86bd7b21fbf5ee1550ac',
        '323181e1482d2a2562d5f694611b6f5710638bbb12a931f8851a42516b56d5ea', 1),
       (7, '020c08e5367fd881e52af43532db814d371b6bd3effb14442ad1044e71c0c0e41a',
        '3cbfd2bdff52b075bca2ac4b1563f85e37426b61548f2fa0b88dcd197974ced1', 1),
       (8, '02aef406b4c4a3c007094a05c2d2a2d815133a41914c96385a2d9ca71529b4d302',
        '65532b6d1f76cdca2ea9b2f79148cceec619483e128e275f65dcbbe0d51b47c7', 1),
       (9, '03bfcc7afddf4f00c043faca2254ca8f09e3109c20b830d44a9b4438b363b9865e',
        'ccf058eaffdbe2342be184e756cd1113b8c2cdc5fc798749b85a3e8ea7c76924', 1),
       (10, '03b49d9a127c271fad4bcdf88bd9fb3430b122044972654dfe78a754c5e3064f4f',
        'ec61d844445b537670c5e0f5cdba186fea42ae82b9e4326d075f7b4650e32222', 1),
       (11, '03679e387bae8b7b724edc42a8149b7aa426edfc9ad54a1fc5e717ab081aca4daf',
        '1f249047c5a131052c84b10cf57fcd55ddd2dd6cf97d783740dff9c663f955d1', 1),
       (12, '036a1a1a6e952083beb1eb5213168288592cd000b42502bd4b8b1e74a465a2eacc',
        'db30186f43b94394bed6ffddff6a2c8733c5209d9bc741f76509f689e360af43', 1),
       (13, '029137a16dcea3967e8fd46dff0d812a2e60a57bef3eb6a7007867c0496631c5d6',
        'ed76d226e982f3f1ce8eed712ee9ac3e509d4a9c3a5b78d3ff07e8f36de9286c', 1),
       (14, '03a9623189c1da22cec1338d2ab0a982e51794aefb45107d7c4c000a09fc772204',
        'ffa38e7ba4e97a934b061ccd8436cdb99cc9cb6c7439c77ad1a34912183068c0', 1),
       (15, '0283d909d2a886e9274f76f0460625e72674222b6a2bc937071858aa76a6e08d78',
        '2b82d2fb83b2ac396ea52f49cc8b5f2db95f501cf0923161d6a9aaf1c449f41c', 1),
       (16, '02f8f3aca6fbf37e7dfd4cf55cf6a1dcffa2cc6cb0c2e513f8121dfb4d861bf04e',
        '375d9e4f7214aa41906caebe2fc8c01285d4dfc7b40edc339c5241484a6b81f6', 1),
       (17, '039745d56241820f2a385c77aca013ecdff0b9fdce01d3f45ed34752cc9aa62cda',
        '191c109afc4f1bcf8b223d8870ba2e2ba5598ad964358569267388384ffeb184', 1),
       (18, '02e7cb6589255a6e153d181c19aa8a34c5c0e6cef0c0374e0c8ba4b5f36ccfc18a',
        '2c0d01396421417cdd60f83d886942f7af60019c14fdce2c0b8427666e380427', 1),
       (19, '02c2aca26e916926fce4101f0633009ae1c8c97e3081b3779880f6683ea258599c',
        'f868a36953973199612e107b6c464b51a8ff73ead418a3d810792eba1634c20a', 1),
       (20, '027d26f614afe8b6b3c8efb861c6666985701d76efa70c9d5a02f44c1e0be804ab',
        '4b7cb3938a94888fa29d6ff34817ddff85c21ca8e2022178dc8fc914c0cfaaa7', 1),
       (21, '0225aebdbb8ea2d8c401a638c87da670d5e2f0e4fdb9197f09ae75b2c805046724',
        'a858188aed22713edeca4568692d59678a1d6be7d2eb4be2545e5fdc2ddbf9c3', 1),
       (22, '02e20add31fbf82b1369e8f2f537df9225a05d6fd497e2f9c65ee9b2df176c01c8',
        'e5770eb3b611db6d3d926f77c5325a5cfb24788bea0d599369207e99773b1caa', 1);

INSERT INTO account_states (id, balance, vote_for, storage)
VALUES (1, 10000000000000000, NULL, NULL);

INSERT INTO delegate_states (id, rating, wallet_address, create_date)
VALUES (2, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (3, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (4, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (5, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (6, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (7, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (8, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (9, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (10, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (11, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (12, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (13, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (14, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (15, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (16, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (17, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (18, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (19, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (20, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (21, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (22, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021);