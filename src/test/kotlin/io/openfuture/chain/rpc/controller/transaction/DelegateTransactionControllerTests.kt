package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono


@WebFluxTest(DelegateTransactionController::class)
class DelegateTransactionControllerTests : ControllerTests() {

    @MockBean
    private lateinit var service: DelegateTransactionService


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val transactionRequest = DelegateTransactionRequest(1L, 1L, "hash", "senderAddress", "senderPublicKey", "senderSignature",
            "delegateKey")
        val unconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(1L, 1L, "senderAddress", "senderPublicKey", "senderSignature",
            "hash", DelegateTransactionPayload("delegateKey"))
        val expectedResponse = DelegateTransactionResponse(unconfirmedDelegateTransaction)

        given(service.add(transactionRequest)).willReturn(unconfirmedDelegateTransaction)

        val actualResponse = webClient.post().uri("/rpc/transactions/delegates")
            .body(Mono.just(transactionRequest), DelegateTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(DelegateTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

}

