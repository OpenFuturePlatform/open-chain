package io.openfuture.chain.rpc.controller.view

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.delegate.ViewDelegate
import io.openfuture.chain.core.service.ViewDelegateService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.BDDMockito
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl

@WebFluxTest(ViewDelegateController::class)
class ViewDelegateControllerTests : ControllerTests() {

    @MockBean
    private lateinit var viewDelegateService: ViewDelegateService


    @Test
    fun getAllViewsShouldReturnDelegateViewsListTest() {
        val pageDelegates = PageImpl(listOf(ViewDelegate(1, "publicKey", "nodeId", "address", "host", 1, 1, 1, 1)))
        val expectedPageResponse = PageResponse(pageDelegates)

        BDDMockito.given(viewDelegateService.getAll(PageRequest())).willReturn(pageDelegates)

        val actualPageResponse = webClient.get().uri("/rpc/delegates/view")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        Assertions.assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        Assertions.assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["address"]).isEqualTo(expectedPageResponse.list.first().address)
        Assertions.assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["publicKey"]).isEqualTo(expectedPageResponse.list.first().publicKey)
        Assertions.assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["votesCount"]).isEqualTo(expectedPageResponse.list.first().votesCount.toInt())
    }

}
