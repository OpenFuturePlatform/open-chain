package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.service.BaseTransactionService
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.explorer.ExplorerResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean

@WebFluxTest(ExplorerController::class)
class ExplorerControllerTests : ControllerTests() {

    @MockBean
    private lateinit var blockService: BlockService

    @MockBean
    private lateinit var epochService: EpochService

    @MockBean
    private lateinit var networkApiService: NetworkApiService

    @MockBean
    private lateinit var baseTransactionService: BaseTransactionService


    @Test
    fun getExplorerInfoShouldReturnExplorerResponse() {
        val blockCount = 1L
        val nodesCount = 1200
        val epochNumber = 22L
        val blockProductionTime = 2L
        val transactionsCount = 100L
        val epochDate = 1533201843817
        val delegatesCount = 1.toByte()
        val transactionsPerSecond = 17L
        val expectedResponse = ExplorerResponse(nodesCount, blockCount, transactionsCount, blockProductionTime,
            transactionsPerSecond, epochNumber, epochDate, delegatesCount)

        given(blockService.getCount()).willReturn(blockCount)
        given(epochService.getEpochStart()).willReturn(epochDate)
        given(epochService.getEpochIndex()).willReturn(epochNumber)
        given(networkApiService.getNetworkSize()).willReturn(nodesCount)
        given(baseTransactionService.getCount()).willReturn(transactionsCount)
        given(blockService.getAvgProductionTime()).willReturn(blockProductionTime)
        given(baseTransactionService.getProducingPerSecond()).willReturn(transactionsPerSecond)
        given(epochService.getDelegatesPublicKeys()).willReturn(listOf("publicKey"))

        val actualResponse = webClient.get().uri("/rpc/explorer/info")
            .exchange()
            .expectStatus().isOk
            .expectBody(ExplorerResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

}
