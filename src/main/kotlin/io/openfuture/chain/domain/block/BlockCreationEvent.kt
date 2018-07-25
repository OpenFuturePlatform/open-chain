package io.openfuture.chain.domain.block

import io.openfuture.chain.entity.transaction.base.BaseTransaction


class BlockCreationEvent(
    val pendingTransactions: MutableList<BaseTransaction>
)