package io.openfuture.chain.core.model.node

data class RamInfo(
    val free: Long,
    val used: Long,
    val total: Long
)