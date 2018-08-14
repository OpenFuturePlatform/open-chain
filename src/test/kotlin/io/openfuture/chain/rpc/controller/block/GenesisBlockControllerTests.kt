package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl


@WebFluxTest(GenesisBlockController::class)
class GenesisBlockControllerTests : ControllerTests() {

    @MockBean
    private lateinit var service: GenesisBlockService


    @Test
    fun getAllMainBlocksShouldReturnMainBlocksList() {
        val pageGenesisBlocks = PageImpl(listOf(GenesisBlock(1, 1, "previousHash", 1, "hash", "signature", "publicKey",
            GenesisBlockPayload(1, listOf()))))
        val expectedPageResponse = PageResponse(pageGenesisBlocks)

        given(service.getAll(PageRequest())).willReturn(pageGenesisBlocks)

        val actualPageResponse = webClient.get().uri("/rpc/blocks/genesis")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        Assertions.assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        Assertions.assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().publicKey)
        Assertions.assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["previousHash"]).isEqualTo(expectedPageResponse.list.first().previousHash)
    }

}

