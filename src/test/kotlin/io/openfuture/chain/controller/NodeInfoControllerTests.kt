package io.openfuture.chain.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.UptimeResponse
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.node.NodeTimestampResponse
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.HardwareInfoService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean

@WebFluxTest(NodeInfoController::class)
class NodeInfoControllerTests : ControllerTests() {

    @MockBean
    private lateinit var hardwareInfoService: HardwareInfoService

    @Autowired
    private lateinit var nodeProperties: NodeProperties


    @Test
    fun getVersionShouldReturnVersionFromProperties() {
        webClient.get().uri("${PathConstant.RPC}/info/getVersion")
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java).isEqualTo<Nothing>("""{"version":"${nodeProperties.version}"}""")
    }

    @Test
    fun getTimestampShouldReturnCurrentTimestamp() {
        val expectedTimestamp = System.currentTimeMillis()

        val result = webClient.get().uri("${PathConstant.RPC}/info/getTimestamp")
                .exchange()
                .expectStatus().isOk
                .expectBody(NodeTimestampResponse::class.java)
                .returnResult().responseBody!!

        assertThat(result.timestamp).isGreaterThanOrEqualTo(expectedTimestamp)
    }

    @Test
    fun getUptimeShouldReturnApplicationUptime() {
        val result = webClient.get().uri("${PathConstant.RPC}/info/getUptime")
                .exchange()
                .expectStatus().isOk
                .expectBody(UptimeResponse::class.java)
                .returnResult().responseBody!!

        assertThat(result.uptime).isGreaterThan(1L)
    }

    @Test
    fun getHardwareInfoShoutReturnHardwareInfo() {
        val expected = HardwareInfo(
                CpuInfo("1", 1L, 1),
                RamInfo(1L, 1L, 2L),
                1L,
                listOf(NetworkInfo("IN", listOf("192.168.1.1")))
        )

        given(hardwareInfoService.getHardwareInfo()).willReturn(expected)

        val result = webClient.get().uri("${PathConstant.RPC}/info/getHardwareInfo")
                .exchange()
                .expectStatus().isOk
                .expectBody(HardwareInfo::class.java)
                .returnResult().responseBody!!


        assertThat(result).isEqualTo(expected)
    }

}