package io.openfuture.chain.domain.block

import io.openfuture.chain.entity.transaction.Transaction


class BlockCreationEvent(
    val pendingTransactions: MutableList<Transaction>
)