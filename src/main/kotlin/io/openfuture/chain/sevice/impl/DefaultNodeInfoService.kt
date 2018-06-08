package io.openfuture.chain.sevice.impl

import io.openfuture.chain.domain.NodeHardwareInfoResponse
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StoreInfo
import io.openfuture.chain.sevice.NodeInfoService
import org.springframework.stereotype.Service
import oshi.SystemInfo
import java.net.NetworkInterface

@Service
class DefaultNodeInfoService : NodeInfoService {

    private val systemInfo = SystemInfo()
    private val hardwareLayer = systemInfo.hardware

    override fun getHardwareInfo(): NodeHardwareInfoResponse {
        val cpuInfo = getCpuInfo()
        val ramInfo = getRamInfo()
        val storesInfo = getDiskStoresInfo()
        val networksInfo = getNetworksInfo()

        return NodeHardwareInfoResponse(cpuInfo, ramInfo, storesInfo, networksInfo)
    }

    override fun getCpuInfo(): CpuInfo {
        val processor = hardwareLayer.processor
        val cpuModel = processor.model
        val cpuFrequency = processor.vendorFreq
        val cpuNumberOfCores = processor.physicalProcessorCount
        return CpuInfo(cpuModel, cpuFrequency, cpuNumberOfCores)
    }

    override fun getRamInfo(): RamInfo {
        val ram = hardwareLayer.memory
        val freeMemory = ram.available
        val totalMemory = ram.total
        val usedMemory = totalMemory - freeMemory
        return RamInfo(freeMemory, usedMemory, totalMemory)
    }

    override fun getDiskStoresInfo(): List<StoreInfo> {
        val diskStores = hardwareLayer.diskStores
        val stores = ArrayList<StoreInfo>(diskStores.size)
        for (diskStore in diskStores) {
            val totalStore = diskStore.size
            val storageInfoDto = StoreInfo(totalStore)
            stores.add(storageInfoDto)
        }

        return stores
    }

    override fun getNetworksInfo(): List<NetworkInfo> {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
        val networks = ArrayList<NetworkInfo>()
        for (networkInterface in networkInterfaces) {
            val networkInterfaceName = networkInterface.name
            val interfaceAddresses = networkInterface.interfaceAddresses
                    .map { interfaceAddress -> interfaceAddress.address.hostAddress }
                    .toList()

            val networkInfo = NetworkInfo(networkInterfaceName, interfaceAddresses)
            networks.add(networkInfo)
        }

        return networks
    }
}