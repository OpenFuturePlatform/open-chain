INSERT INTO blocks(
  id,
  hash,
  height,
  previous_hash,
  timestamp,
  signature,
  public_key
) VALUES (
  1,
  '838c84179c7e644cdf2ff0af3055ed45c6f43e0bd7634f8bd6ae7d088b1aaf0a',
  1,
  '',
  1532345018021,
  'MEUCIQCLeQuqCrDd8nmS037ZfmQNtpUf/AsfQilmK7CcNNIi7QIgKNdhszih/PezHW52v4/tdsZxaLovJzDnLvUy98tnsgg=',
  '038bbbeeb867b999991cd5b146b392ba2fe44ea69d1cc7208e32190184b13aaf1b'
);

INSERT INTO genesis_blocks (id, epoch_index) VALUES (1, 1);