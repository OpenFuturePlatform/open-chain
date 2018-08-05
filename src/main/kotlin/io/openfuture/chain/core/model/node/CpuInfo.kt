package io.openfuture.chain.core.model.node

data class CpuInfo(
    val model: String,
    val frequency: Long,
    val numberOfCores: Int
)