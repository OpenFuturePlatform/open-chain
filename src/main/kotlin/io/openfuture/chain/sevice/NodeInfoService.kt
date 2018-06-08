package io.openfuture.chain.sevice

import io.openfuture.chain.domain.NodeHardwareInfoResponse
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StoreInfo

interface NodeInfoService {

    fun getHardwareInfo(): NodeHardwareInfoResponse

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStoresInfo(): List<StoreInfo>

    fun getNetworksInfo(): List<NetworkInfo>
}