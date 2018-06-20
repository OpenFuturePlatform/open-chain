package io.openfuture.chain.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.domain.HealthResponse
import io.openfuture.chain.domain.node.NodeTimestampResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NodeInfoControllerTests : ControllerTests() {

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
    fun testGetHealthCheckShoutReturnAppUpTime() {
        val response = HealthResponse(1L)

        val responseResult = webClient.get().uri("${PathConstant.RPC}/info/getHealthCheck")
                .exchange()
                .expectStatus().isOk
                .expectBody(HealthResponse::class.java)
                .returnResult().responseBody!!

        assertThat(responseResult).isNotNull
        assertThat(response.uptime).isGreaterThan(0L)
    }

}