package io.openfuture.chain.domain.block

import io.openfuture.chain.entity.Transaction

class BlockCreationEvent(
    val pendingTransactions: List<Transaction>
)