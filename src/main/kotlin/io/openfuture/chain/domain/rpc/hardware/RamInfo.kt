package io.openfuture.chain.domain.rpc.hardware

data class RamInfo(
    val free: Long,
    val used: Long,
    val total: Long
)