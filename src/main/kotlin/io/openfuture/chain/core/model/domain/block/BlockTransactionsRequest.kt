package io.openfuture.chain.core.model.domain.block

data class BlockTransactionsRequest(
    var voteTransactionsCount: Int = 0,
    var delegateTransactionsCount: Int = 0,
    var transferTransactionsCount: Int = 0
)