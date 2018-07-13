package io.openfuture.chain.domain.rpc

import io.openfuture.chain.domain.rpc.hardware.CpuInfo
import io.openfuture.chain.domain.rpc.hardware.NetworkInfo
import io.openfuture.chain.domain.rpc.hardware.RamInfo

data class HardwareInfo(
        val cpu: CpuInfo,
        val ram: RamInfo,
        val totalStorageSize: Long,
        val networks: List<NetworkInfo>
)