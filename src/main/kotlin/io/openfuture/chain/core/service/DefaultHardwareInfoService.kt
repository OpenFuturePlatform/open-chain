package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.node.*
import org.springframework.stereotype.Service
import oshi.SystemInfo
import java.net.NetworkInterface
import java.util.stream.LongStream

@Service
class DefaultHardwareInfoService : HardwareInfoService {

    private val hardwareLayer = SystemInfo().hardware


    override fun getHardwareInfo(): HardwareInfo {
        val cpuInfo = getCpuInfo()
        val ramInfo = getRamInfo()
        val diskStorageInfo = getDiskStorageInfo()
        val networksInfo = getNetworksInfo()

        val storageSize = diskStorageInfo.stream().flatMapToLong { LongStream.of(it.totalStorage) }.sum()

        return HardwareInfo(cpuInfo, ramInfo, storageSize, networksInfo)
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

    override fun getDiskStorageInfo(): List<StorageInfo> {
        val diskStores = hardwareLayer.diskStores
        val storage = ArrayList<StorageInfo>(diskStores.size)
        for (diskStore in diskStores) {
            val deviceName = diskStore.name
            val totalStore = diskStore.size
            val storageInfoDto = StorageInfo(deviceName, totalStore)
            storage.add(storageInfoDto)
        }

        return storage
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