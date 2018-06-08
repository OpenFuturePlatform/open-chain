package io.openfuture.chain.sevice.impl

import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StoreInfo
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultNodeInfoServiceTest {

    @InjectMocks
    private val defaultNodeInfoService: DefaultNodeInfoService? = null

    @Test
    fun testGetHardwareInfo() {
        val hardwareInfo = defaultNodeInfoService!!.getHardwareInfo()

        val cpuInfo = hardwareInfo.cpu
        val ramInfo = hardwareInfo.ram
        val diskStoresInfo = hardwareInfo.diskStores
        val networksInfo = hardwareInfo.networks

        Assertions.assertThat(hardwareInfo).isNotNull

        assertCpuInfo(cpuInfo)
        assertRamInfo(ramInfo)
        assertStoresInfo(diskStoresInfo)
        assertNetworksInfo(networksInfo)
    }

    @Test
    fun testGetCpuInfo() {
        val cpuInfo = defaultNodeInfoService!!.getCpuInfo()

        assertCpuInfo(cpuInfo)
    }

    @Test
    fun testGetRamInfo() {
        val ramInfo = defaultNodeInfoService!!.getRamInfo()

        assertRamInfo(ramInfo)
    }

    @Test
    fun testGetStoresInfo() {
        val storesInfo = defaultNodeInfoService!!.getDiskStoresInfo()

        assertStoresInfo(storesInfo)
    }

    @Test
    fun testGetNetworksInfo() {
        val networksInfo = defaultNodeInfoService!!.getNetworksInfo()

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

    private fun assertStoresInfo(diskStoresInfo: List<StoreInfo>) {
        Assertions.assertThat(diskStoresInfo).isNotEmpty
        for (diskStoreInfo in diskStoresInfo) {
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