package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl

@WebFluxTest(MainBlockController::class)
class MainBlockControllerTests : ControllerTests() {

    @MockBean
    private lateinit var service: MainBlockService


    @Test
    fun getAllMainBlocksShouldReturnMainBlocksList() {
        val pageMainBlocks = PageImpl(listOf(MainBlock(1, 1, "previousHash", 1, "hash", "signature", "publicKey",
            MainBlockPayload("merkleHash"))))
        val expectedPageResponse = PageResponse(pageMainBlocks)

        given(service.getAll(PageRequest())).willReturn(pageMainBlocks)

        val actualPageResponse = webClient.get().uri("/rpc/blocks/main")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().publicKey)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["previousHash"]).isEqualTo(expectedPageResponse.list.first().previousHash)
    }

}
