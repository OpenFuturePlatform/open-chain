package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.block.MainBlock

class MainBlockDto(
        var hash: String,
        var height: Long,
        var previousHash: String,
        var merkleHash: String,
        var timestamp: Long,
        var signature: String,
        var transactions: Set<TransactionDto>
)