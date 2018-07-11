package io.openfuture.chain.events

import io.openfuture.chain.entity.Transaction

class BlockCreationEvent(
    val pendingTransactions: List<Transaction>
)