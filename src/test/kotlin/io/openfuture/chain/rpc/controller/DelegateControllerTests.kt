package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.DelegateResponse
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl


@WebFluxTest(DelegateController::class)
class DelegateControllerTests : ControllerTests() {

    @MockBean
    private lateinit var delegateService: DelegateService

    @MockBean
    private lateinit var genesisBlockService: GenesisBlockService


    @Test
    fun getAllShouldReturnDelegatesListTest() {
        val pageDelegates = PageImpl(listOf(Delegate("publicKey", "address", "host", 1)))
        val expectedPageResponse = PageResponse(pageDelegates)

        given(delegateService.getAll(PageRequest())).willReturn(pageDelegates)

        val actualPageResponse = webClient.get().uri("/rpc/delegates")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["address"]).isEqualTo(expectedPageResponse.list.first().address)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().publicKey)
    }

    @Test
    fun getAllActiveShouldReturnActiveDelegatesListTest() {
        val delegate = Delegate("publicKey", "address", "host", 1)
        val genesisBlock = GenesisBlock(1, 1, "previousHash", 1, "hash", "signature", "publicKey",
            GenesisBlockPayload(1, listOf(delegate)))
        val expectedResponse = listOf(DelegateResponse(delegate))

        given(genesisBlockService.getLast()).willReturn(genesisBlock)

        val actualResponse = webClient.get().uri("/rpc/delegates/active")
            .exchange()
            .expectStatus().isOk
            .expectBody(List::class.java)
            .returnResult().responseBody!!

        assertThat((actualResponse[0] as LinkedHashMap<*, *>)["address"]).isEqualTo(expectedResponse.first().address)
        assertThat((actualResponse[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedResponse.first().publicKey)
    }

}
