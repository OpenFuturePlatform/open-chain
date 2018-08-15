package io.openfuture.chain.rpc.domain.explorer

data class ExplorerResponse(
        var nodesCount: Int? = null,
        var blocksCount: Long? = null,
        var transactionsCount: Long? = null,
        var secondsPerBlock: Long? = null,
        var transactionsPerSecond: Long? = null,
        var currentEpochNumber: Long? = null,
        var currentEpochDate: Long? = null,
        var delegatesCount: Byte? = null
)
