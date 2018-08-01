package io.openfuture.chain.rpc.domain.node

data class CpuInfo(
    val model: String,
    val frequency: Long,
    val numberOfCores: Int
)