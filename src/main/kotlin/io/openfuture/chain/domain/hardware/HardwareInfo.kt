package io.openfuture.chain.domain.hardware

data class CpuInfo(
        val model: String,
        val frequency: Long,
        val numberOfCores: Int
)

data class NetworkInfo(
        val interfaceName: String,
        val addresses: List<String>
)

data class RamInfo(
        val free: Long,
        val used: Long,
        val total: Long
)

data class StorageInfo(
        val name: String,
        val totalStorage: Long
)