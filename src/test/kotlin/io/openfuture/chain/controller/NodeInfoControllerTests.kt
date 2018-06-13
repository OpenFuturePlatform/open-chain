package io.openfuture.chain.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.domain.HealthResponse
import io.openfuture.chain.domain.NodeTimestampResponse
import io.openfuture.chain.domain.NodeVersionResponse
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.util.AppContextUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NodeInfoController::class)
class NodeInfoControllerTests : ControllerTests() {

    @Autowired
    private lateinit var context: ApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var nodeProperties: NodeProperties

    @Before
    fun setUp() {
        val version = "1"
        AppContextUtils.context = this.context
        given(nodeProperties.version).willReturn(version)
    }

    @Test
    fun getVersionShouldReturnVersion() {
        val response = NodeVersionResponse()
        var responseString = objectMapper.writeValueAsString(response)

        val responseJsonResult = mvc.perform(get("${PathConstant.RPC}/info/getVersion"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

        assertThat(responseString).isEqualTo(responseJsonResult)
    }

    @Test
    fun getTimestampShouldReturnTimestampNow() {
        val response = NodeTimestampResponse(System.currentTimeMillis())

        val responseJsonResult = mvc.perform(get("${PathConstant.RPC}/info/getTimestamp"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        val responseResult = objectMapper.readValue(responseJsonResult, NodeTimestampResponse::class.java)

        assertThat(response.version).isEqualTo(responseResult.version)
        assertThat(response.timestamp).isLessThanOrEqualTo(responseResult.timestamp)
    }

    @Test
    fun testGetHealthCheckShoutReturnAppUpTime() {
        val response = HealthResponse(1L)

        val responseJsonResult = mvc.perform(get("${PathConstant.RPC}/info/getHealthCheck"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        val responseResult = objectMapper.readValue(responseJsonResult, HealthResponse::class.java)

        assertThat(responseResult).isNotNull
        assertThat(response.upTime).isGreaterThan(0L)
    }

}