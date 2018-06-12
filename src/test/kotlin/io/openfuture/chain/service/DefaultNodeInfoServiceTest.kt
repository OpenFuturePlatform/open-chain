package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class DefaultNodeInfoServiceTest : ServiceTests() {

    private lateinit var defaultNodeInfoService: DefaultNodeInfoService

    @Before
    fun setUp() {
        defaultNodeInfoService = DefaultNodeInfoService()
    }

    @Test
    fun testGetHardwareInfo() {
        val hardwareInfo = defaultNodeInfoService.getHardwareInfo()

        val cpuInfo = hardwareInfo.cpu
        val ramInfo = hardwareInfo.ram
        val totalStorageSize = hardwareInfo.totalStorageSize
        val networksInfo = hardwareInfo.networks

        Assertions.assertThat(hardwareInfo).isNotNull

        assertCpuInfo(cpuInfo)
        assertRamInfo(ramInfo)
        Assertions.assertThat(totalStorageSize).isGreaterThan(0L)
        assertNetworksInfo(networksInfo)
    }

    @Test
    fun testGetCpuInfo() {
        val cpuInfo = defaultNodeInfoService.getCpuInfo()

        assertCpuInfo(cpuInfo)
    }

    @Test
    fun testGetRamInfo() {
        val ramInfo = defaultNodeInfoService.getRamInfo()

        assertRamInfo(ramInfo)
    }

    @Test
    fun testGetStoresInfo() {
        val diskStorageInfo = defaultNodeInfoService.getDiskStorageInfo()

        assertStorageInfo(diskStorageInfo)
    }

    @Test
    fun testGetNetworksInfo() {
        val networksInfo = defaultNodeInfoService.getNetworksInfo()

        assertNetworksInfo(networksInfo)
    }

    private fun assertCpuInfo(cpuInfo: CpuInfo) {
        Assertions.assertThat(cpuInfo).isNotNull

        Assertions.assertThat(cpuInfo.frequency).isGreaterThan(0L)
        Assertions.assertThat(cpuInfo.model).isNotBlank()
        Assertions.assertThat(cpuInfo.numberOfCores).isGreaterThan(0)
    }

    private fun assertRamInfo(ramInfo: RamInfo) {
        Assertions.assertThat(ramInfo).isNotNull

        Assertions.assertThat(ramInfo.free).isGreaterThan(0L)
        Assertions.assertThat(ramInfo.used).isGreaterThan(0L)
        Assertions.assertThat(ramInfo.total).isGreaterThan(0L)
    }

    private fun assertStorageInfo(diskStorageInfo: List<StorageInfo>) {
        Assertions.assertThat(diskStorageInfo).isNotEmpty
        for (diskStoreInfo in diskStorageInfo) {
            Assertions.assertThat(diskStoreInfo.totalStorage).isGreaterThan(0L)
        }
    }

    private fun assertNetworksInfo(networksInfo: List<NetworkInfo>) {
        Assertions.assertThat(networksInfo).isNotEmpty
        for (networkInfo in networksInfo) {
            Assertions.assertThat(networkInfo.interfaceName).isNotBlank()

            val addresses = networkInfo.addresses
            for (address in addresses) {
                Assertions.assertThat(address).isNotBlank()
            }
        }
    }

}