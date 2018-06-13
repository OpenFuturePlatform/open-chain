package io.openfuture.chain.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.config.ControllerTests
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
    fun testGetVersion() {
        val response = NodeVersionResponse()

        val responseJson = mvc.perform(get("${PathConstant.RPC}/info/getVersion"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

        assertThat(responseJson).isEqualTo(objectMapper.writeValueAsString(response))
    }

    @Test
    fun testGetTimestamp() {
        val response = NodeTimestampResponse(System.currentTimeMillis())

        val responseJson = mvc.perform(get("${PathConstant.RPC}/info/getTimestamp"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        val responseResult = objectMapper.readValue(responseJson, NodeTimestampResponse::class.java)

        assertThat(responseResult.version).isEqualTo(response.version)
        assertThat(responseResult.timestamp).isGreaterThanOrEqualTo(response.timestamp)
    }

    @Test
    fun testGetHealthCheck() {
        val timeUpString = mvc.perform(get("${PathConstant.RPC}/info/getHealthCheck"))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
        val timeUp = timeUpString.toLong()

        assertThat(timeUp).isGreaterThan(0L)
    }

}