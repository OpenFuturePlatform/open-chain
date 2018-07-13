package io.openfuture.chain.domain

import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo

data class HardwareInfo(
    val cpu: CpuInfo,
    val ram: RamInfo,
    val totalStorageSize: Long,
    val networks: List<NetworkInfo>
)