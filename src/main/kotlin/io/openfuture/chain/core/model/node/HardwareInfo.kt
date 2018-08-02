package io.openfuture.chain.core.model.node

data class HardwareInfo(
    val cpu: CpuInfo,
    val ram: RamInfo,
    val totalStorageSize: Long,
    val networks: List<NetworkInfo>
)