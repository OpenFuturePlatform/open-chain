package io.openfuture.chain.sevice.impl

import io.openfuture.chain.domain.NodeHardwareInfoResponse
import io.openfuture.chain.domain.hardware.*
import io.openfuture.chain.sevice.NodeInfoService
import org.springframework.stereotype.Service
import oshi.SystemInfo
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.collections.ArrayList

@Service
class DefaultNodeInfoService : NodeInfoService {

    override fun getHardwareInfo(): NodeHardwareInfoResponse {
        val systemInfo = SystemInfo()
        val hardwareLayer = systemInfo.hardware

        val processor = hardwareLayer.processor
        val cpuModel = processor.model
        val cpuFrequency = processor.vendorFreq
        val cpuNumberOfCores = processor.physicalProcessorCount
        val cpu = CpuInfoDto(cpuModel, cpuFrequency, cpuNumberOfCores)

        val memory = hardwareLayer.memory
        val memoryType = "n / a"
        val freeMemory = memory.available
        val totalMemory = memory.total
        val usedMemory = totalMemory - freeMemory
        val ramInfoDto = RamInfoDto(memoryType, freeMemory, usedMemory, totalMemory)

        val stores = ArrayList<StorageInfoDto>()
        val diskStores = hardwareLayer.diskStores
        for (diskStore in diskStores) {
            val storageType = "n / a"
            val freeStorage = -1L
            val usableStorage = -1L
            val totalStore = diskStore.size
            val storageInfoDto = StorageInfoDto(storageType, freeStorage, -usableStorage, totalStore)
            stores.add(storageInfoDto)
        }

        val localAddress = InetAddress.getLocalHost().hostAddress

        val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
        val networkInterfaceInfoDtoList = ArrayList<NetworkInterfaceInfoDto>()
        for (networkInterface in networkInterfaces) {
            val networkInterfaceName = networkInterface.name
            val interfaceAddresses = networkInterface.interfaceAddresses
                    .map { interfaceAddress -> interfaceAddress.address.hostAddress }
                    .toList()

            val networkInterfaceInfoDto = NetworkInterfaceInfoDto(networkInterfaceName, interfaceAddresses)
            networkInterfaceInfoDtoList.add(networkInterfaceInfoDto)
        }

        val networkInfoDto = NetworkInfoDto(
                Arrays.asList(localAddress),
                Arrays.asList("n / a"),
                networkInterfaceInfoDtoList
        )

        return NodeHardwareInfoResponse(cpu, ramInfoDto, stores, networkInfoDto)
    }

}