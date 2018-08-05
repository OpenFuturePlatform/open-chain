package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.controller.transaction.DelegateTransactionController
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono


@WebFluxTest(DelegateTransactionController::class)
class DelegateTransactionControllerTest : ControllerTests() {

    @MockBean
    private lateinit var service: DelegateTransactionService


    @Test
    fun doDeriveHashReturnBytesOfPayload() {
        val payload = DelegateTransactionPayload(1L, "delegateKey")
        val bytes = ByteArray(1)

        given(service.getBytes(payload)).willReturn(bytes)

        val result = webClient.post().uri("/rpc/transactions/delegates/doGenerateHash")
            .body(Mono.just(payload), DelegateTransactionPayload::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(bytes)
    }

    @Test
    fun addTransaction() {
        val transactionRequest = DelegateTransactionRequest("senderAddress", "senderPublicKey", "senderSignature",
            1L, "delegateKey")
        val transactionDto = UDelegateTransaction(1, "senderAddress", "senderPublicKey", "senderSignature",
            "hash", DelegateTransactionPayload(1L, "delegateKey"))

        given(service.add(transactionRequest)).willReturn(transactionDto)

        val result = webClient.post().uri("/rpc/transactions/votes/doGenerateHash")
            .body(Mono.just(transactionRequest), DelegateTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(DelegateTransactionResponse(transactionDto))
    }

}

