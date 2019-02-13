package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.block.MainBlockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl

@WebFluxTest(MainBlockController::class)
class MainBlockControllerTests : ControllerTests() {

    @MockBean
    private lateinit var blockManager: BlockManager

    @MockBean
    private lateinit var transactionManager: TransactionManager

    @MockBean
    private lateinit var epochService: EpochService

    companion object {
        private const val MAIN_BLOCK_URL = "/rpc/blocks/main"
    }


    @Test
    fun getAllMainBlocksShouldReturnMainBlocksList() {
        val pageMainBlocks = PageImpl(listOf(createMainBlock()))
        val expectedPageResponse = PageResponse(pageMainBlocks)

        given(blockManager.getAllMainBlocks(PageRequest())).willReturn(pageMainBlocks)

        val actualPageResponse = webClient.get().uri(MAIN_BLOCK_URL)
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().publicKey)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["previousHash"]).isEqualTo(expectedPageResponse.list.first().previousHash)
    }

    @Test
    fun getMainBlockByHashShouldReturnMainBlockWithCurrentHash() {
        val hash = "hash"
        val transactionCount = 5L
        val epochIndex = 1L
        val mainBlock = createMainBlock()
        val expectedMainBlockResponse = MainBlockResponse(mainBlock, transactionCount, epochIndex)

        given(blockManager.getMainBlockByHash(hash)).willReturn(mainBlock)
        given(transactionManager.getCountByBlock(mainBlock)).willReturn(transactionCount)
        given(epochService.getEpochByBlock(mainBlock)).willReturn(epochIndex)

        val actualMainBlockResponse = webClient.get().uri("$MAIN_BLOCK_URL/$hash")
            .exchange()
            .expectStatus().isOk
            .expectBody(MainBlockResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualMainBlockResponse).isEqualToComparingFieldByField(expectedMainBlockResponse)
    }

    @Test
    fun getNextMainBlockByHashShouldReturnMainBlockWithNextHash() {
        val hash = "hash"
        val transactionCount = 5L
        val epochIndex = 1L
        val mainBlock = createMainBlock()
        val expectedMainBlockResponse = MainBlockResponse(mainBlock, transactionCount, epochIndex)

        given(blockManager.getNextMainBlock(hash)).willReturn(mainBlock)
        given(transactionManager.getCountByBlock(mainBlock)).willReturn(transactionCount)
        given(epochService.getEpochByBlock(mainBlock)).willReturn(epochIndex)

        val actualMainBlockResponse = webClient.get().uri("$MAIN_BLOCK_URL/$hash/next")
            .exchange()
            .expectStatus().isOk
            .expectBody(MainBlockResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualMainBlockResponse).isEqualToComparingFieldByField(expectedMainBlockResponse)
    }

    @Test
    fun getPreviousMainBlockByHashShouldReturnMainBlockWithPreviousHash() {
        val hash = "hash"
        val transactionCount = 5L
        val epochIndex = 1L
        val mainBlock = createMainBlock()
        val expectedMainBlockResponse = MainBlockResponse(mainBlock, transactionCount, epochIndex)

        given(blockManager.getPreviousMainBlock(hash)).willReturn(mainBlock)
        given(transactionManager.getCountByBlock(mainBlock)).willReturn(transactionCount)
        given(epochService.getEpochByBlock(mainBlock)).willReturn(epochIndex)

        val actualMainBlockResponse = webClient.get().uri("$MAIN_BLOCK_URL/$hash/previous")
            .exchange()
            .expectStatus().isOk
            .expectBody(MainBlockResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualMainBlockResponse).isEqualToComparingFieldByField(expectedMainBlockResponse)
    }

    private fun createMainBlock(): MainBlock =
        MainBlock(1, 1, "previousHash", "hash", "signature",
            "publicKey", MainBlockPayload("merkleHash", "stateHash", "receiptHash"))
            .apply { id = 1 }

}
