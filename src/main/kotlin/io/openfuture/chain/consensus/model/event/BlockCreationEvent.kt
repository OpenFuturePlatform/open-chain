package io.openfuture.chain.consensus.model.event

import io.openfuture.chain.core.model.entity.transaction.UTransaction

class BlockCreationEvent(
    val pendingTransactions: MutableList<UTransaction>
)