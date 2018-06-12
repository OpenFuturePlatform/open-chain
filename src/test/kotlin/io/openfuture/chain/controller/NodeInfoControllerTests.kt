package io.openfuture.chain.controller

import io.openfuture.chain.config.ControllerTests
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

    @MockBean
    private lateinit var nodeProperties: NodeProperties

    @Before
    fun setUp() {
        AppContextUtils.context = this.context
    }

    @Test
    fun testGetVersion() {
        val testVersion = "1"
        val testContentValue = "{\"version\":\"1\"}"

        given(nodeProperties.version).willReturn(testVersion)

        val content = mvc.perform(get("${PathConstant.RPC}/info/getVersion"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        assertThat(content).isEqualTo(testContentValue)
    }

    @Test
    fun testGetTimestamp() {

    }

    @Test
    fun testGetHealthCheck() {

    }

}