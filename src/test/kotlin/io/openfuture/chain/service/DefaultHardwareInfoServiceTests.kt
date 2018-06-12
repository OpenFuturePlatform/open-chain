package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DefaultHardwareInfoServiceTests : ServiceTests() {

    private lateinit var service: DefaultHardwareInfoService

    @Before
    fun setUp() {
        service = DefaultHardwareInfoService()
    }

    @Test
    fun testGetHardwareInfo() {
        val hardwareInfo = service.getHardwareInfo()

        var totalStorageSize = 0L
        val storageInfo = service.getDiskStorageInfo()
        for (storage in storageInfo) {
            totalStorageSize += storage.totalStorage
        }

        assertThat(hardwareInfo.cpu).isEqualTo(service.getCpuInfo())
        assertThat(hardwareInfo.ram).isNotNull
        assertThat(hardwareInfo.networks).isEqualTo(service.getNetworksInfo())
        assertThat(hardwareInfo.totalStorageSize).isEqualTo(totalStorageSize)
    }

    @Test
    fun testGetCpuInfo() {
        val cpuInfo = service.getCpuInfo()

        assertThat(cpuInfo).isNotNull
        assertThat(cpuInfo.frequency).isGreaterThan(0L)
        assertThat(cpuInfo.model).isNotBlank()
        assertThat(cpuInfo.numberOfCores).isGreaterThan(0)
    }

    @Test
    fun testGetRamInfo() {
        val ramInfo = service.getRamInfo()

        assertThat(ramInfo).isNotNull
        assertThat(ramInfo.free).isGreaterThan(0L)
        assertThat(ramInfo.used).isGreaterThan(0L)
        assertThat(ramInfo.total).isGreaterThan(0L)
    }

    @Test
    fun testGetStoresInfo() {
        val diskStorageInfo: List<StorageInfo> = service.getDiskStorageInfo()

        assertThat(diskStorageInfo).isNotEmpty
        for (diskStoreInfo in diskStorageInfo) {
            assertThat(diskStoreInfo.totalStorage).isGreaterThan(0L)
        }
    }

    @Test
    fun testGetNetworksInfo() {
        val networksInfo: List<NetworkInfo> = service.getNetworksInfo()

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