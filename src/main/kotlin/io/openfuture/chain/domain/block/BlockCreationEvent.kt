package io.openfuture.chain.domain.block

import io.openfuture.chain.entity.transaction.BaseTransaction

class BlockCreationEvent(
    val pendingTransactions: List<BaseTransaction>
)