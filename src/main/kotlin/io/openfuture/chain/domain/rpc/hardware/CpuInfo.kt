package io.openfuture.chain.domain.rpc.hardware

data class CpuInfo(
        val model: String,
        val frequency: Long,
        val numberOfCores: Int
)