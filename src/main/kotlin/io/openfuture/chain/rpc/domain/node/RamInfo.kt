package io.openfuture.chain.rpc.domain.node

data class RamInfo(
    val free: Long,
    val used: Long,
    val total: Long
)