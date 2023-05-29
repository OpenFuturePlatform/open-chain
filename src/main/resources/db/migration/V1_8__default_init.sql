INSERT INTO blocks (id, TIMESTAMP, height, previous_hash, hash, signature, public_key)
VALUES (1,
        1532345018021,
        0,
        '',
        'bee2cf33664f31034b56e6b0d32c2d07f2703bff34732813779d620e842291e5',
        'MEUCIDDUiKHns8pOffBLctj6gShKuCk762SdFiGiVADDJvPUAiEAi4HilxXDCQJj2RUktKKbHKOqZMVCtTIej+2lMZinWbE=',
        '0395e3e1b612344bbafdca5aa94de6d072835bc9af76c70cbc1e2aa33deb4c4c93'),

        (2,
        1532345018021,
        1,
        'bee2cf33664f31034b56e6b0d32c2d07f2703bff34732813779d620e842291e5',
        '7fe9320b2fb90f5b9bb7e109029a8dfbb727b91c44003b2d915dd733a4f1e86e',
        'MEYCIQCJtgrNJkeCuiBtFA03ab/ChME0exzBzEAAUsLU5siZZAIhAJwslTRn+KVbH5nRjzaNfRmDCsTvK+Fa7fwEX8kOd1we',
        '0395e3e1b612344bbafdca5aa94de6d072835bc9af76c70cbc1e2aa33deb4c4c93');

INSERT INTO main_blocks (id, transaction_merkle_hash, state_merkle_hash, receipt_merkle_hash)
VALUES (1, '', '5449cc7a0afb3161a58cf2189fdf8da7386b65eb14ddcaf341c13716ea310e47', '');

INSERT INTO genesis_blocks (id, epoch_index)
       VALUES (2, 1);

INSERT INTO delegate2genesis (public_key, genesis_id)
VALUES ('02179657d318d008aa6ccc53678e14900ebccdec9552a7dc3ba78f8693ac7d9419', 2),
       ('03f1f2460e52a85d800d166a8349ee94b3b63ccb4f94d3194f1ddf855891812673', 2),
       ('02d04ba87d7d4b919aaf8fe9d9ca5043e0efda948b506f5ffa5c8f17dce035a2a1', 2);;

INSERT INTO states (id, address, hash)
VALUES (1, '0x0000000000000000000000000000000000000000',
        'f4639eccbed9ef1b911aed167b38ced71f525ff0b92e44d797a3ef6e856b788c'),
       (2, '02179657d318d008aa6ccc53678e14900ebccdec9552a7dc3ba78f8693ac7d9419',
        '07e4bcc3aa017a3cfc370fa26c7bdc735c49a7f0000489260caadb077289bb38'),
       (3, '03f1f2460e52a85d800d166a8349ee94b3b63ccb4f94d3194f1ddf855891812673',
        'a98111345037c3bb230351dc6ae9c2c69809497b69f3c1227ace6a2b344063da'),
       (4, '02d04ba87d7d4b919aaf8fe9d9ca5043e0efda948b506f5ffa5c8f17dce035a2a1',
        '5929237c69bc41d05aae61d0e303fe5d71595d57b0fa70ec69a1707cbbf86202');

INSERT INTO account_states (id, balance, vote_for, storage)
VALUES (1, 10000000000000000, NULL, NULL);

INSERT INTO delegate_states (id, rating, wallet_address, create_date)
VALUES (2, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (3, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021),
       (4, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', 1532345018021);