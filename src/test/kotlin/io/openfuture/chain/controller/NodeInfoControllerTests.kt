package io.openfuture.chain.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.domain.HealthResponse
import io.openfuture.chain.domain.node.NodeTimestampResponse
import io.openfuture.chain.domain.node.NodeVersionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NodeInfoControllerTests : ControllerTests() {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun getVersionShouldReturnVersion() {
        val response = NodeVersionResponse()
        val responseString = objectMapper.writeValueAsString(response)

        val responseBytesResult = webClient.get().uri("${PathConstant.RPC}/info/getVersion")
                .exchange()
                .expectStatus().isOk
                .expectBody(NodeVersionResponse::class.java)
                .returnResult().responseBodyContent
        val responseJsonResult = String(responseBytesResult!!)

        assertThat(responseString).isEqualTo(responseJsonResult)
    }

    @Test
    fun getTimestampShouldReturnTimestampNow() {
        val response = NodeTimestampResponse(System.currentTimeMillis())

        val responseByteResult = webClient.get().uri("${PathConstant.RPC}/info/getTimestamp")
                .exchange()
                .expectStatus().isOk
                .expectBody(NodeTimestampResponse::class.java)
                .returnResult().responseBodyContent
        val responseResult = objectMapper.readValue(responseByteResult, NodeTimestampResponse::class.java)

        assertThat(response.version).isEqualTo(responseResult.version)
        assertThat(response.timestamp).isLessThanOrEqualTo(responseResult.timestamp)
    }

    @Test
    fun testGetHealthCheckShoutReturnAppUpTime() {
        val response = HealthResponse(1L)

        val responseByteResult = webClient.get().uri("${PathConstant.RPC}/info/getHealthCheck")
                .exchange()
                .expectStatus().isOk
                .expectBody(HealthResponse::class.java)
                .returnResult().responseBodyContent
        val responseResult = objectMapper.readValue(responseByteResult, HealthResponse::class.java)

        assertThat(responseResult).isNotNull
        assertThat(response.upTime).isGreaterThan(0L)
    }

}