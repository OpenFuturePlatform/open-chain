package io.openfuture.chain.domain

import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StoreInfo

data class NodeHardwareInfoResponse(
        val cpu: CpuInfo,
        val ram: RamInfo,
        val diskStores: List<StoreInfo>,
        val networks: List<NetworkInfo>
)