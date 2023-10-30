INSERT INTO blocks (id, timestamp, height, previous_hash, hash, signature, public_key)
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
VALUES ('02b04aa1832e799503000a6b8da1cdbb737c167fc829472c726a12ad9a4ccf24eb', 2),
       ('02c6847fcdc0239581151d1b05e7004c331eba097ae381349220c7cb0c5f5df9b3', 2),
       ('02c4aedc4a7e2d8cc0e73e6dfb428e8555adc8a1b74207cb61143babd3e04be63f', 2);

INSERT INTO states (id, address, hash)
VALUES (1, '0xBd844A9E5a551b1887617b23D78b3435a0811ca0 ',
        'f4639eccbed9ef1b911aed167b38ced71f525ff0b92e44d797a3ef6e856b788c'),
       (2, '02b04aa1832e799503000a6b8da1cdbb737c167fc829472c726a12ad9a4ccf24eb',
        '07e4bcc3aa017a3cfc370fa26c7bdc735c49a7f0000489260caadb077289bb38'),
       (3, '02c6847fcdc0239581151d1b05e7004c331eba097ae381349220c7cb0c5f5df9b3',
        'a98111345037c3bb230351dc6ae9c2c69809497b69f3c1227ace6a2b344063da'),
       (4, '02c4aedc4a7e2d8cc0e73e6dfb428e8555adc8a1b74207cb61143babd3e04be63f',
        '5929237c69bc41d05aae61d0e303fe5d71595d57b0fa70ec69a1707cbbf86202'),
       (5, '0x6f7c626D720044905536009AD0c637625e5F57F5 ',
        '5929237c69bc41d05aae61d0e303fe5d71595d57b0fa70ec69a1707cbbf86205');

INSERT INTO account_states (id, balance, vote_for, storage)
VALUES (1, 1000000000000, NULL, NULL),
       (5, 10000000, NULL, NULL);

INSERT INTO delegate_states (id, rating, wallet_address, create_date)
VALUES (2, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb ', 1532345018021),
       (3, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb ', 1532345018021),
       (4, 0, '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb ', 1532345018021);