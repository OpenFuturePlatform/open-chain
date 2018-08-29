package io.openfuture.chain.core.model.domain.block

data class BlockTransactionsRequest(
    var votesCount: Long = 0,
    var delegatesCount: Long = 0,
    var transferCount: Long = 0
)