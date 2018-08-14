package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.vote.VoteTransactionPayload
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono

@WebFluxTest(VoteTransactionController::class)
class VoteTransactionControllerTests : ControllerTests() {

    @MockBean
    private lateinit var service: VoteTransactionService


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val transactionRequest = VoteTransactionRequest(1L, 1L, "senderAddress", 1,
            "delegateKey", "senderSignature", "senderPublicKey")
        val unconfirmedVoteTransaction = UnconfirmedVoteTransaction(1L, 1L, "senderAddress", "senderPublicKey", "senderSignature",
            "hash", VoteTransactionPayload(1, "delegateKey"))
        val expectedResponse = VoteTransactionResponse(unconfirmedVoteTransaction)

        given(service.add(transactionRequest)).willReturn(unconfirmedVoteTransaction)

        val actualResponse = webClient.post().uri("/rpc/transactions/votes")
            .body(Mono.just(transactionRequest), VoteTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(VoteTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

}
