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
VALUES ('03ae0cfa7f1664d02cd4f95b0ee0397c3b37b860106ab7e62bbd45e357d43a4cb6', 2),
       ('03f3ae70cb893a1b218e008cf79ee6a5ee69f4d93b6342b676ff1bfd64310e06eb', 2),
       ('03a0ed2889f622a425f7a2ee4333ffe889d79120e476751ee2f1be4e2a82d57e46', 2),
       ('027a9d6808669051bf1aa589c7b6852ec3b984f864b3f75e3d2e845d380bc6ede2', 2),
       ('033108a31f1cd8cdcdbe50cbcd66d6f41af719645b4b5b414220a637e796365bc0', 2);

INSERT INTO states (id, address, hash)
VALUES (1, '0x0000000000000000000000000000000000000000','f4639eccbed9ef1b911aed167b38ced71f525ff0b92e44d797a3ef6e856b788c'),
       (2, '03ae0cfa7f1664d02cd4f95b0ee0397c3b37b860106ab7e62bbd45e357d43a4cb6','07e4bcc3aa017a3cfc370fa26c7bdc735c49a7f0000489260caadb077289bb38'),
       (3, '03f3ae70cb893a1b218e008cf79ee6a5ee69f4d93b6342b676ff1bfd64310e06eb','a98111345037c3bb230351dc6ae9c2c69809497b69f3c1227ace6a2b344063da'),
       (4, '03a0ed2889f622a425f7a2ee4333ffe889d79120e476751ee2f1be4e2a82d57e46', '5929237c69bc41d05aae61d0e303fe5d71595d57b0fa70ec69a1707cbbf86202'),
       (5, '027a9d6808669051bf1aa589c7b6852ec3b984f864b3f75e3d2e845d380bc6ede2', '5929237c69bc41d05aae61d0e303fe5d71595d57b0fa70ec69a1707cbbf86204'),
       (6, '033108a31f1cd8cdcdbe50cbcd66d6f41af719645b4b5b414220a637e796365bc0', '5929237c69bc41d05aae61d0e303fe5d71595d57b0fa70ec69a1707cbbf86205'),
       (7, '0xEA1b30BD5Ba790787dd795Ec5cdb815E43958681', '5eb92a6447824c42bff3ab53b0af3cb5cca7072d558199de8ba815aa6b3474b2');

INSERT INTO account_states (id, balance, vote_for, storage)
VALUES
    (1, 10000000000000000, NULL, NULL),
    (7, 10000000000000000, NULL, NULL);

INSERT INTO delegate_states (id, rating, wallet_address, create_date)
VALUES (2, 0, '0xEA1b30BD5Ba790787dd795Ec5cdb815E43958681', 1685962401000),
       (3, 0, '0xb6D847780B3736362f12eB46bf043C11c0028135', 1685962401000),
       (4, 0, '0x23f340F3E78A4BD302B73f622DB4d0631DbFef03', 1685962401000),
       (5, 0, '0xdcE75a918a14d3fa152fA48C9f16111ba7b05c3D', 1685962401000),
       (6, 0, '0x7A65eEdB1b77157d5D9Fc099A00CBDf7c3333642', 1685962401000);