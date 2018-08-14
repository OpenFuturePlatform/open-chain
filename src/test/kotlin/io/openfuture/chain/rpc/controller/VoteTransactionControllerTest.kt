package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.rpc.controller.transaction.VoteTransactionController
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionRequest
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

    @MockBean
    private lateinit var nodeClock: NodeClock


    @Test
    fun doDeriveHashReturnBytesOfPayload() {
        val hashRequest = VoteTransactionHashRequest(1L, 1L, "senderAddress", 1, "delegateKey")
        val hash = ByteArray(1).toString()

        given(service.generateHash(hashRequest)).willReturn(hash)

        val result = webClient.post().uri("/rpc/transactions/votes/doGenerateHash")
            .body(Mono.just(hashRequest), VoteTransactionHashRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(hash)
    }

    @Test
    fun addTransaction() {
        val transactionRequest = VoteTransactionRequest(1L, 1L, "senderAddress", VoteType.FOR,
            "delegateKey", "senderSignature", "senderPublicKey")
        val unconfirmedVoteTransaction = UnconfirmedVoteTransaction(TransactionHeader(1L, 1L, "senderAddress"),
            "hash", "senderSignature", "senderPublicKey", VoteTransactionPayload(1, "delegateKey"))

        given(service.add(transactionRequest)).willReturn(unconfirmedVoteTransaction)
        given(nodeClock.networkTime()).willReturn(1L)

        val result = webClient.post().uri("/rpc/transactions/votes")
            .body(Mono.just(transactionRequest), VoteTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(VoteTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualToComparingFieldByField(VoteTransactionResponse(unconfirmedVoteTransaction))
    }

}
