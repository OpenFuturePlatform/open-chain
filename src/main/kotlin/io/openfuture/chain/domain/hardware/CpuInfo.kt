package io.openfuture.chain.domain.hardware

data class CpuInfo(
    val model: String,
    val frequency: Long,
    val numberOfCores: Int
)