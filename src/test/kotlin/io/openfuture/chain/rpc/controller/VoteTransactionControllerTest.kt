package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono

@WebFluxTest(VoteTransactionController::class)
class VoteTransactionControllerTest : ControllerTests() {

    @MockBean
    private lateinit var service: VoteTransactionService


    @Test
    fun doDeriveHashReturnBytesOfPayload() {
        val payload = VoteTransactionPayload(1L, 1, "delegateKey")
        val bytes = ByteArray(1)

        given(service.getBytes(payload)).willReturn(bytes)

        val result = webClient.post().uri("/rpc/transactions/votes/doGenerateHash")
            .body(Mono.just(payload), VoteTransactionPayload::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(bytes)
    }

    @Test
    fun addTransaction() {
        val transactionRequest = VoteTransactionRequest("senderAddress", "senderPublicKey", "senderSignature",
            1L, 1, "delegateKey")
        val transactionDto = UVoteTransaction(1, "senderAddress", "senderPublicKey", "senderSignature",
            "hash", VoteTransactionPayload(1L, 1, "delegateKey"))

        given(service.add(transactionRequest)).willReturn(transactionDto)

        val result = webClient.post().uri("/rpc/transactions/votes/doGenerateHash")
            .body(Mono.just(transactionRequest), VoteTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(VoteTransactionResponse(transactionDto))
    }

}
