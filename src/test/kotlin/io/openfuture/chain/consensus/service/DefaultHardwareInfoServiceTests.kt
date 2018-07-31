package io.openfuture.chain.consensus.service

import io.openfuture.chain.config.ServiceTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DefaultHardwareInfoServiceTests : ServiceTests() {

    private val service = DefaultHardwareInfoService()


    @Test
    fun getHardwareInfoShouldReturnCpuRamStorageNetworkInformation() {
        val hardwareInfo = service.getHardwareInfo()

        assertThat(hardwareInfo).isNotNull
        with(hardwareInfo) {
            assertThat(cpu).isEqualTo(service.getCpuInfo())
            assertThat(ram).isNotNull
            assertThat(networks).isEqualTo(service.getNetworksInfo())


            var storageSize = 0L
            val diskStorageInfo = service.getDiskStorageInfo()
            for (disk in diskStorageInfo) {
                storageSize += disk.totalStorage
            }

            assertThat(totalStorageSize).isEqualTo(storageSize)
        }
    }

    @Test
    fun getCpuInfoShouldReturnCpuInformation() {
        val cpuInfo = service.getCpuInfo()

        assertThat(cpuInfo).isNotNull
        assertThat(cpuInfo.frequency).isGreaterThan(0L)
        assertThat(cpuInfo.model).isNotBlank()
        assertThat(cpuInfo.numberOfCores).isGreaterThan(0)
    }

    @Test
    fun getRamInfoShouldReturnRamInformation() {
        val ramInfo = service.getRamInfo()

        assertThat(ramInfo).isNotNull
        assertThat(ramInfo.free).isGreaterThan(0L)
        assertThat(ramInfo.used).isGreaterThan(0L)
        assertThat(ramInfo.total).isGreaterThan(0L)
    }

    @Test
    fun getStorageInfoShouldReturnStorageInformation() {
        val diskStorageInfo = service.getDiskStorageInfo()

        assertThat(diskStorageInfo).isNotEmpty
        for (diskStoreInfo in diskStorageInfo) {
            assertThat(diskStoreInfo.totalStorage).isGreaterThan(0L)
        }
    }

    @Test
    fun getNetworksInfoShouldNetworkInformation() {
        val networksInfo = service.getNetworksInfo()

        assertThat(networksInfo).isNotEmpty
        for (networkInfo in networksInfo) {
            assertThat(networkInfo.interfaceName).isNotBlank()

            val addresses = networkInfo.addresses
            for (address in addresses) {
                assertThat(address).isNotBlank()
            }
        }
    }

}