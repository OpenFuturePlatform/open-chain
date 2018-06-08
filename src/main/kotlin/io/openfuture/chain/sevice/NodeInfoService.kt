package io.openfuture.chain.sevice

import io.openfuture.chain.domain.NodeHardwareInfoResponse
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo

interface NodeInfoService {

    fun getHardwareInfo(): NodeHardwareInfoResponse

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>
}