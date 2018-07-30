package io.openfuture.chain.rpc.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.rpc.domain.ResponseHeader
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.rpc.domain.node.CpuInfo
import io.openfuture.chain.rpc.domain.node.HardwareInfo
import io.openfuture.chain.rpc.domain.node.NetworkInfo
import io.openfuture.chain.rpc.domain.node.RamInfo
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

    @MockBean
    private lateinit var nodeClock: NodeClock

    @MockBean
    private lateinit var nodeProperties: NodeProperties


    @Test
    fun getVersionShouldReturnVersionFromProperties() {
        val expectedVersion = "0"
        val expectedBody = getRestResponse(expectedVersion)
        given(nodeProperties.version).willReturn(expectedVersion)

        val result = webClient.get().uri("/rpc/info/getVersion")
            .exchange()
            .expectStatus().isOk
            .expectBody(expectedBody::class.java)
            .returnResult().responseBody!!

        assertThat(expectedVersion).isEqualTo(result.body)
    }

    @Test
    fun getTimestampShouldReturnCurrentTimestamp() {
        val expectedTimestamp = System.currentTimeMillis()
        val expectedBody = getRestResponse(expectedTimestamp)

        given(nodeClock.nodeTime()).willReturn(expectedTimestamp)

        val result = webClient.get().uri("/rpc/info/getTimestamp")
                .exchange()
                .expectStatus().isOk
                .expectBody(expectedBody.javaClass)
                .returnResult().responseBody!!

        assertThat(expectedTimestamp).isGreaterThanOrEqualTo(result.body)
    }

    @Test
    fun getUptimeShouldReturnApplicationUptime() {
        val expectedTimestamp = System.currentTimeMillis()
        val expectedBody = getRestResponse(expectedTimestamp)

        given(nodeClock.nodeTime()).willReturn(expectedTimestamp)

        val result = webClient.get().uri("/rpc/info/getUptime")
                .exchange()
                .expectStatus().isOk
                .expectBody(expectedBody::class.java)
                .returnResult().responseBody!!

        assertThat(1L).isLessThan(result.body)
    }

    @Test
    fun getHardwareInfoShoutReturnHardwareInfo() {
        val expected = HardwareInfo(
            CpuInfo("1", 1L, 1),
            RamInfo(1L, 1L, 2L),
            1L,
            listOf(NetworkInfo("IN", listOf("192.168.1.1")))
        )
        val expectedResponse = getRestResponse(expected)

        given(hardwareInfoService.getHardwareInfo()).willReturn(expected)

        val result = webClient.get().uri("/rpc/info/getHardwareInfo")
                .exchange()
                .expectStatus().isOk
                .expectBody(expectedResponse::class.java)
                .returnResult().responseBody!!

        assertThat(ObjectMapper().writeValueAsString(result.body))
            .isEqualTo(ObjectMapper().writeValueAsString(expectedResponse.body))
    }

    private fun <T> getRestResponse(body: T): RestResponse<T> {
        given(nodeClock.networkTime()).willReturn(0)
        given(nodeProperties.version).willReturn("0")

        return RestResponse(ResponseHeader(0, "0"), body)
    }

}