package io.openfuture.chain.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.UptimeResponse
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.node.NodeTimestampResponse
import io.openfuture.chain.service.HardwareInfoService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean

@WebFluxTest(NodeInfoController::class)
class NodeInfoControllerTests : ControllerTests() {

    @MockBean
    private lateinit var hardwareInfoService: HardwareInfoService

    @Test
    fun getVersionShouldReturnVersion() {
        val responseString = """{"version":"1.0.0"}"""

        webClient.get().uri("${PathConstant.RPC}/info/getVersion")
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java).isEqualTo<Nothing>(responseString)
    }

    @Test
    fun getTimestampShouldReturnTimestampNow() {
        val response = NodeTimestampResponse(System.currentTimeMillis())

        val responseResult = webClient.get().uri("${PathConstant.RPC}/info/getTimestamp")
                .exchange()
                .expectStatus().isOk
                .expectBody(NodeTimestampResponse::class.java)
                .returnResult().responseBody!!

        assertThat(response.version).isEqualTo(responseResult.version)
        assertThat(response.timestamp).isLessThanOrEqualTo(responseResult.timestamp)
    }

    @Test
    fun getHealthCheckShoutReturnAppUpTime() {
        val response = UptimeResponse(1L)

        val responseResult = webClient.get().uri("${PathConstant.RPC}/info/getUptime")
                .exchange()
                .expectStatus().isOk
                .expectBody(UptimeResponse::class.java)
                .returnResult().responseBody!!

        assertThat(responseResult).isNotNull
        assertThat(response.uptime).isGreaterThan(0L)
    }

    @Test
    fun getHardwareInfoShoutReturnHardwareInfo() {
        val cpuInfo = CpuInfo("1", 1L, 1)
        val ramInfo = RamInfo(1L, 1L, 2L)
        val networksInfo = listOf(NetworkInfo("IN", listOf("192.168.1.1")))
        val totalStorageSize = 1L
        val hardwareInfo = HardwareInfo(cpuInfo, ramInfo, totalStorageSize, networksInfo)

        given(hardwareInfoService.getHardwareInfo()).willReturn(hardwareInfo)

        webClient.get().uri("${PathConstant.RPC}/info/getHardwareInfo")
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java).isEqualTo<Nothing>(ObjectMapper().writeValueAsString(hardwareInfo))
    }

}