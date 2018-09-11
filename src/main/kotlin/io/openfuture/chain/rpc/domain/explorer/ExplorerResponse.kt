package io.openfuture.chain.rpc.domain.explorer

data class ExplorerResponse(
    val nodesCount: Int,
    val blocksCount: Long,
    val transactionsCount: Long,
    val blockProductionTime: Long,
    val transactionsPerSecond: Long,
    val currentEpochNumber: Long,
    val currentEpochDate: Long,
    val delegatesCount: Byte
)
