package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.service.DelegateService
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


    @Test
    fun getAllShouldReturnDelegatesListTest() {
        val pageDelegates = PageImpl(listOf(Delegate("publicKey", "address")))
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

}
