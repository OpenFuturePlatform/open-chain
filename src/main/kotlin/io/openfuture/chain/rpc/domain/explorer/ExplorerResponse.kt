package io.openfuture.chain.rpc.domain.explorer

import java.util.*

class ExplorerDto(
    var nodesCount: Long,
    var blocksCount: Long,
    var transactionsCount: Long,
    var blocksSpeed: Double,
    var transactionsSpeed: Double,
    var currectEpochNumber: Long,
    var currectEpochDate: Date,
    var delegatesCount: Long
)
