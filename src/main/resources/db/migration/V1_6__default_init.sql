INSERT INTO blocks (id, TIMESTAMP, height, previous_hash, hash, signature, public_key)
VALUES (1,
        1532345018021,
        1,
        '',
        '838c84179c7e644cdf2ff0af3055ed45c6f43e0bd7634f8bd6ae7d088b1aaf0a',
        'MEUCIQCLeQuqCrDd8nmS037ZfmQNtpUf/AsfQilmK7CcNNIi7QIgKNdhszih/PezHW52v4/tdsZxaLovJzDnLvUy98tnsgg=',
        '038bbbeeb867b999991cd5b146b392ba2fe44ea69d1cc7208e32190184b13aaf1b');

INSERT INTO genesis_blocks (id, epoch_index)
VALUES (1, 1);

INSERT INTO wallets (id, address, balance)
VALUES (1, '0x0000000000000000000000000000000000000000', 10000000000000000);


INSERT INTO delegates (id, public_key, node_id, address, host, port, registration_date)
VALUES (1, '02b04aa1832e799503000a6b8da1cdbb737c167fc829472c726a12ad9a4ccf24eb',
       'e1980f62f593c33d293a3c40e1a23d4624ebd2efaa7e0e4ab6251c55890d8f68',
       '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', '192.168.88.111', 9191, 1532345018021),
       (2, '02c6847fcdc0239581151d1b05e7004c331eba097ae381349220c7cb0c5f5df9b3',
       '9e5a3a23642e28b59e15e9c002abb43a136d609dd01cc48d1ac86bd73ecbba2b',
       '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', '192.168.88.111', 9192, 1532345018021),
       (3, '02c4aedc4a7e2d8cc0e73e6dfb428e8555adc8a1b74207cb61143babd3e04be63f',
       'dd8b9747571f6195b71ab1e1808c4e8ba931047088c071cf8631fac37c506b00',
       '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', '192.168.88.111', 9193, 1532345018021),
       (4, '02203492b48445da0f7392f6fa88d902f71d1b3714740ed99f43009a70fd7f6da8',
       'f7f2261208c8f9c95c9fe795fa0d163542dbeafb21835d6a153356901d5cf8cd',
       '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', '192.168.88.111', 9194, 1532345018021),
       (5, '029a9b6a44d2e322af6884a00660d63ab80effceb0a80f86bd7b21fbf5ee1550ac',
       '6fea4cf9aacd52f8d103d16665c638b52ba74ebc23bc516d87d3bb1e18f7bc18',
       '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', '192.168.88.111', 9195, 1532345018021),
       (6, '020c08e5367fd881e52af43532db814d371b6bd3effb14442ad1044e71c0c0e41a',
       'c45e0be2ae0d297d763ed3671cd0b462001cf2f568e91da055f48038c952e6bf',
       '0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb', '192.168.88.111', 9196, 1532345018021);

INSERT INTO delegate2genesis (delegate_id, genesis_id)
VALUES (1, 1),
       (2, 1),
       (3, 1),
       (4, 1),
       (5, 1),
       (6, 1)
