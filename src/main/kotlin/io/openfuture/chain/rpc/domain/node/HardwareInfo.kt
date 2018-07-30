package io.openfuture.chain.rpc.domain.node

data class HardwareInfo(
    val cpu: CpuInfo,
    val ram: RamInfo,
    val totalStorageSize: Long,
    val networks: List<NetworkInfo>
)