package io.openfuture.chain.consensus.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.core.service.DefaultHardwareInfoService
import io.openfuture.chain.crypto.util.HashUtils
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Test

class DefaultHardwareInfoServiceTests() : ServiceTests() {

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

    @Test fun s()  {
        val a= listOf("02b04aa1832e799503000a6b8da1cdbb737c167fc829472c726a12ad9a4ccf24eb",
        "02c6847fcdc0239581151d1b05e7004c331eba097ae381349220c7cb0c5f5df9b3",
        "02c4aedc4a7e2d8cc0e73e6dfb428e8555adc8a1b74207cb61143babd3e04be63f",
        "02203492b48445da0f7392f6fa88d902f71d1b3714740ed99f43009a70fd7f6da8",
        "029a9b6a44d2e322af6884a00660d63ab80effceb0a80f86bd7b21fbf5ee1550ac",
        "020c08e5367fd881e52af43532db814d371b6bd3effb14442ad1044e71c0c0e41a",
        "02aef406b4c4a3c007094a05c2d2a2d815133a41914c96385a2d9ca71529b4d302",
        "03bfcc7afddf4f00c043faca2254ca8f09e3109c20b830d44a9b4438b363b9865e",
        "03b49d9a127c271fad4bcdf88bd9fb3430b122044972654dfe78a754c5e3064f4f",
         "03679e387bae8b7b724edc42a8149b7aa426edfc9ad54a1fc5e717ab081aca4daf",
         "036a1a1a6e952083beb1eb5213168288592cd000b42502bd4b8b1e74a465a2eacc",
         "029137a16dcea3967e8fd46dff0d812a2e60a57bef3eb6a7007867c0496631c5d6",
         "03a9623189c1da22cec1338d2ab0a982e51794aefb45107d7c4c000a09fc772204",
         "0283d909d2a886e9274f76f0460625e72674222b6a2bc937071858aa76a6e08d78",
         "02f8f3aca6fbf37e7dfd4cf55cf6a1dcffa2cc6cb0c2e513f8121dfb4d861bf04e",
         "039745d56241820f2a385c77aca013ecdff0b9fdce01d3f45ed34752cc9aa62cda",
         "02e7cb6589255a6e153d181c19aa8a34c5c0e6cef0c0374e0c8ba4b5f36ccfc18a",
         "02c2aca26e916926fce4101f0633009ae1c8c97e3081b3779880f6683ea258599c",
         "027d26f614afe8b6b3c8efb861c6666985701d76efa70c9d5a02f44c1e0be804ab",
        "0225aebdbb8ea2d8c401a638c87da670d5e2f0e4fdb9197f09ae75b2c805046724",
         "02e20add31fbf82b1369e8f2f537df9225a05d6fd497e2f9c65ee9b2df176c01c8")
        a.forEach { s->System.out.println(ByteUtils.toHexString(HashUtils.sha256(ByteUtils.fromHexString(s))))}
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