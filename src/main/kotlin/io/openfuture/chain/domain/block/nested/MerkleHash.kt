package io.openfuture.chain.domain.block.nested

import io.openfuture.chain.domain.transaction.TransactionDto

data class MerkleHash(
    val hash: String,
    val transactions: List<TransactionDto>
)