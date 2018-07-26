package io.openfuture.chain.domain.block

import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction


class BlockCreationEvent(
    val pendingTransactions: MutableList<UTransaction>
)