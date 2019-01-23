package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.service.DelegateService
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
    private lateinit var delegateService: DelegateService

    @MockBean
    private lateinit var delegateStateService: DelegateStateService

    @MockBean
    private lateinit var walletStateService: WalletStateService

    @MockBean
    private lateinit var genesisBlockService: GenesisBlockService


    @Test
    fun getAllShouldReturnDelegatesListTest() {
        val delegate = Delegate("publicKey", "address", 1)
        val block = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey",
            MainBlockPayload("merkleHash", "stateHash"))
        val delegates = listOf(DelegateState("publicKey", block, 1))
        val expectedPageResponse = PageResponse(PageImpl(listOf(delegate)))

        given(delegateStateService.getAllDelegates(PageRequest())).willReturn(delegates)
        given(delegateService.getByPublicKey("publicKey")).willReturn(delegate)

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
        val publicKey = "publicKey"
        val delegate = Delegate(publicKey, "address", 1)
        val genesisBlock = GenesisBlock(1, 1, "previousHash", "hash", "signature", "publicKey",
            GenesisBlockPayload(1, mutableListOf(publicKey)))
        val expectedPageResponse = PageResponse(PageImpl(listOf(delegate)))

        given(genesisBlockService.getLast()).willReturn(genesisBlock)
        given(delegateService.getByPublicKey(publicKey)).willReturn(delegate)

        val actualPageResponse = webClient.get().uri("/rpc/delegates/active")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["address"]).isEqualTo(expectedPageResponse.list.first().address)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().publicKey)

    }

}
