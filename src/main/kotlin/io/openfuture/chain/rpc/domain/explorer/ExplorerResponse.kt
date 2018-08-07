package io.openfuture.chain.rpc.domain.explorer

class ExplorerResponse(
    var nodesCount: Long,
    var blocksCount: Long,
    var transactionsCount: Long,
    var blocksSpeed: Double,
    var transactionsSpeed: Double,
    var currectEpochNumber: Long,
    var currectEpochDate: Long,
    var delegatesCount: Byte
)
