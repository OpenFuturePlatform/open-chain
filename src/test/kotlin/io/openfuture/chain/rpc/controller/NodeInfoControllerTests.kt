package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.node.CpuInfo
import io.openfuture.chain.core.model.node.HardwareInfo
import io.openfuture.chain.core.model.node.NetworkInfo
import io.openfuture.chain.core.model.node.RamInfo
import io.openfuture.chain.core.service.HardwareInfoService
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
    private lateinit var nodeKeyHolder: NodeKeyHolder

    @MockBean
    private lateinit var nodeConfigurator: NodeConfigurator

    companion object {
        private const val NODE_INFO_URL = "/rpc/info"
    }


    @Test
    fun getUptimeShouldReturnApplicationUptime() {
        val expectedTimestamp = System.currentTimeMillis()

        val actualTimeStamp = webClient.get().uri("$NODE_INFO_URL/getUptime")
                .exchange()
                .expectStatus().isOk
                .expectBody(Long::class.java)
                .returnResult().responseBody!!

        assertThat(actualTimeStamp).isLessThan(expectedTimestamp)
    }

    @Test
    fun getHardwareInfoShoutReturnHardwareInfo() {
        val expectedHardwareInfo = HardwareInfo(
            CpuInfo("1", 1L, 1),
            RamInfo(1L, 1L, 2L),
            1L,
            listOf(NetworkInfo("IN", listOf("192.168.1.1")))
        )

        given(hardwareInfoService.getHardwareInfo()).willReturn(expectedHardwareInfo)

        val actualHardwareInfo = webClient.get().uri("$NODE_INFO_URL/getHardwareInfo")
                .exchange()
                .expectStatus().isOk
                .expectBody(HardwareInfo::class.java)
                .returnResult().responseBody!!

        assertThat(actualHardwareInfo).isEqualTo(expectedHardwareInfo)
    }

}