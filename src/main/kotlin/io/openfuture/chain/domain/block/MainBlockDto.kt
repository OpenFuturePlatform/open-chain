package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.transaction.BaseTransactionDto

class MainBlockDto(
    var hash: String,
    var height: Long,
    var previousHash: String,
    var merkleHash: String,
    var timestamp: Long,
    var signature: String,
    var transactions: Set<BaseTransactionDto>
)