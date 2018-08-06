package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.controller.transaction.DelegateTransactionController
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionHashRequest
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
        val hashRequest = DelegateTransactionHashRequest(1L, 1L, "delegateKey")
        val hash = ByteArray(1).toString()

        given(service.generateHash(hashRequest)).willReturn(hash)

        val result = webClient.post().uri("/rpc/transactions/delegates/doGenerateHash")
            .body(Mono.just(hashRequest), DelegateTransactionHashRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(hash)
    }

    @Test
    fun addTransaction() {
        val transactionRequest = DelegateTransactionRequest(1L, 1L, "senderAddress", "senderPublicKey", "senderSignature",
            "delegateKey")
        val transactionDto = UnconfirmedDelegateTransaction(1L, 1L, "senderAddress", "senderPublicKey", "senderSignature",
            "hash", DelegateTransactionPayload("delegateKey"))

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

