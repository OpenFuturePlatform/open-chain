package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.rpc.controller.transaction.TransferTransactionController
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionRequest
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
        val hashRequest = TransferTransactionHashRequest(1L, 1L, "senderAddress", 1L, "recipientAddress")
        val hash = ByteArray(1).toString()

        given(service.generateHash(hashRequest)).willReturn(hash)

        val result = webClient.post().uri("/rpc/transactions/transfer/doGenerateHash")
            .body(Mono.just(hashRequest), TransferTransactionHashRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(hash)
    }

    @Test
    fun addTransaction() {
        val transactionRequest = TransferTransactionRequest(1L, 1L, "senderAddress", "senderPublicKey", "senderSignature",
            1, "recipientAddress")
        val unconfirmedTransferTransaction = UnconfirmedTransferTransaction(1L, 1L, "senderAddress", "senderPublicKey", "senderSignature",
            "hash", TransferTransactionPayload(1L, "delegateKey"))

        given(service.add(transactionRequest)).willReturn(unconfirmedTransferTransaction)

        val result = webClient.post().uri("/rpc/transactions/transfer")
            .body(Mono.just(transactionRequest), TransferTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(TransferTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualToComparingFieldByField(TransferTransactionResponse(unconfirmedTransferTransaction))
    }

}
