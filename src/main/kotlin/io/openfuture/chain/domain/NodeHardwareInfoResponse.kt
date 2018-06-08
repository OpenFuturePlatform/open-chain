package io.openfuture.chain.domain

import io.openfuture.chain.domain.hardware.CpuInfoDto
import io.openfuture.chain.domain.hardware.NetworkInfoDto
import io.openfuture.chain.domain.hardware.RamInfoDto
import io.openfuture.chain.domain.hardware.StorageInfoDto

data class NodeHardwareInfoResponse(
        val cpu: CpuInfoDto,
        val ram: RamInfoDto,
        val diskStores: List<StorageInfoDto>,
        val network: NetworkInfoDto
)