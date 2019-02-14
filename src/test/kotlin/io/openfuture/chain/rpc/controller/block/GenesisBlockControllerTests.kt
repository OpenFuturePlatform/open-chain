package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.block.GenesisBlockResponse
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl


@WebFluxTest(GenesisBlockController::class)
class GenesisBlockControllerTests : ControllerTests() {

    @MockBean
    private lateinit var blockManager: BlockManager

    companion object {
        private const val GENESIS_BLOCK_URL = "/rpc/blocks/genesis"
    }


    @Test
    fun getAllGenesisBlocksShouldReturnGenesisBlocksList() {
        val pageGenesisBlocks = PageImpl(listOf(GenesisBlock(1, 1, "previousHash",
            "hash", "signature", "publicKey", GenesisBlockPayload(1L, mutableListOf()))))
        val expectedPageResponse = PageResponse(pageGenesisBlocks)

        given(blockManager.getAllGenesisBlocks(PageRequest())).willReturn(pageGenesisBlocks)

        val actualPageResponse = webClient.get().uri("/rpc/blocks/genesis")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().publicKey)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["previousHash"]).isEqualTo(expectedPageResponse.list.first().previousHash)
    }

    @Test
    fun getGenesisBlockByHashShouldReturnGenesisBlockWithCurrentHash() {
        val hash = "hash"
        val genesisBlock = createGenesisBlock()
        val expectedGenesisBlockResponse = GenesisBlockResponse(genesisBlock)

        given(blockManager.getGenesisBlockByHash(hash)).willReturn(genesisBlock)

        val actualGenesisBlockResponse = webClient.get().uri("$GENESIS_BLOCK_URL/$hash")
            .exchange()
            .expectStatus().isOk
            .expectBody(GenesisBlockResponse::class.java)
            .returnResult().responseBody!!

        Assertions.assertThat(actualGenesisBlockResponse).isEqualToComparingFieldByField(expectedGenesisBlockResponse)
    }

    @Test
    fun getNextGenesisBlockByHashShouldReturnGenesisBlockWithNextHash() {
        val hash = "hash"
        val genesisBlock = createGenesisBlock()
        val expectedGenesisBlockResponse = GenesisBlockResponse(genesisBlock)

        given(blockManager.getNextGenesisBlock(hash)).willReturn(genesisBlock)

        val actualGenesisBlockResponse = webClient.get().uri("$GENESIS_BLOCK_URL/$hash/next")
            .exchange()
            .expectStatus().isOk
            .expectBody(GenesisBlockResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualGenesisBlockResponse).isEqualToComparingFieldByField(expectedGenesisBlockResponse)
    }

    @Test
    fun getPreviousGenesisBlockByHashShouldReturnGenesisBlockWithPreviousHash() {
        val hash = "hash"
        val genesisBlock = createGenesisBlock()
        val expectedGenesisBlockResponse = GenesisBlockResponse(genesisBlock)

        given(blockManager.getPreviousGenesisBlock(hash)).willReturn(genesisBlock)

        val actualGenesisBlockResponse = webClient.get().uri("$GENESIS_BLOCK_URL/$hash/previous")
            .exchange()
            .expectStatus().isOk
            .expectBody(GenesisBlockResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualGenesisBlockResponse).isEqualToComparingFieldByField(expectedGenesisBlockResponse)
    }

    private fun createGenesisBlock(): GenesisBlock =
        GenesisBlock(1, 1, "previousHash", "hash", "signature",
            "publicKey", GenesisBlockPayload(1, mutableListOf())).apply { id = 1 }

}

