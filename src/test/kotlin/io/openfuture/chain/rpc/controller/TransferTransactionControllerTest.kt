package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.TransferTransactionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono

@WebFluxTest(TransferTransactionController::class)
class TransferTransactionControllerTest : ControllerTests() {

    @MockBean
    private lateinit var service: TransferTransactionService


    @Test
    fun doDeriveHashReturnBytesOfPayload() {
        val payload = TransferTransactionPayload(1L, 1, "delegateKey")
        val bytes = ByteArray(1)

        given(service.getBytes(payload)).willReturn(bytes)

        val result = webClient.post().uri("/rpc/transactions/transfer/doGenerateHash")
            .body(Mono.just(payload), TransferTransactionPayload::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(bytes)
    }

    @Test
    fun addTransaction() {
        val transactionRequest = TransferTransactionRequest("senderAddress", "senderPublicKey", "senderSignature",
            1L, 1, "delegateKey")
        val transactionDto = UTransferTransaction(1, "senderAddress", "senderPublicKey", "senderSignature",
            "hash", TransferTransactionPayload(1L, 1, "delegateKey"))

        given(service.add(transactionRequest)).willReturn(transactionDto)

        val result = webClient.post().uri("/rpc/transactions/votes/doGenerateHash")
            .body(Mono.just(transactionRequest), TransferTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(TransferTransactionResponse(transactionDto))
    }

}
