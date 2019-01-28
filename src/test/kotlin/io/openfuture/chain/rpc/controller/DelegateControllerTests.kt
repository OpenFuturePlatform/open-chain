package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.WalletStateService
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
    private lateinit var delegateStateService: DelegateStateService

    @MockBean
    private lateinit var walletStateService: WalletStateService

    @MockBean
    private lateinit var genesisBlockService: GenesisBlockService


    @Test
    fun getAllShouldReturnDelegatesListTest() {
        val block = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey",
            MainBlockPayload("merkleHash", "stateHash"))
        val delegate = DelegateState("publicKey", block, 1, "address", 1532345018021)
        val delegates = listOf(delegate)
        val expectedPageResponse = PageResponse(PageImpl(listOf(delegate)))

        given(delegateStateService.getAllDelegates(PageRequest())).willReturn(delegates)

        val actualPageResponse = webClient.get().uri("/rpc/delegates")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["address"]).isEqualTo(expectedPageResponse.list.first().walletAddress)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().address)
    }

    @Test
    fun getAllActiveShouldReturnActiveDelegatesListTest() {
        val publicKey = "publicKey"
        val genesisBlock = GenesisBlock(1, 1, "previousHash", "hash", "signature", "publicKey",
            GenesisBlockPayload(1, mutableListOf(publicKey)))
        val delegate = DelegateState("publicKey", MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey",
        MainBlockPayload("merkleHash", "stateHash")), 1, "address", 1532345018021)
        val expectedPageResponse = PageResponse(PageImpl(listOf(delegate)))

        given(genesisBlockService.getLast()).willReturn(genesisBlock)
        given(delegateStateService.getLastByAddress(publicKey)).willReturn(delegate)

        val actualPageResponse = webClient.get().uri("/rpc/delegates/active")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["address"]).isEqualTo(expectedPageResponse.list.first().walletAddress)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().address)

    }

}
