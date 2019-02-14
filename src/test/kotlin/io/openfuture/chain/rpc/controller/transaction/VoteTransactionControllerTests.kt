package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.config.any
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
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
    private lateinit var transactionManager: TransactionManager

    companion object {
        private const val VOTE_TRANSACTION_URL = "/rpc/transactions/vote"
        private const val WALLET_ADDRESS = "0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb"
    }


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val request = VoteTransactionRequest(1L, 1L, "hash", WALLET_ADDRESS, 1,
            "delegateKey", "senderSignature", "senderPublicKey")
        val unconfirmedVoteTransaction = UnconfirmedVoteTransaction.of(request)
        val expectedResponse = VoteTransactionResponse(unconfirmedVoteTransaction)

        given(transactionManager.add(any(UnconfirmedVoteTransaction::class.java))).willReturn(unconfirmedVoteTransaction)

        val actualResponse = webClient.post().uri(VOTE_TRANSACTION_URL)
            .body(Mono.just(request), VoteTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(VoteTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

    @Test
    fun getTransactionByHashShouldReturnTransaction() {
        val hash = "hash"
        val mainBlock = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey",
            MainBlockPayload("merkleHash", "stateHash", "receiptHash")).apply { id = 1 }
        val payload = VoteTransactionPayload(1, "delegateKey")
        val expectedTransaction = VoteTransaction(1L, 1L, WALLET_ADDRESS, "hash", "senderSignature", "senderPublicKey",
            payload, mainBlock).apply { id = 1 }
        val expectedResponse = VoteTransactionResponse(expectedTransaction)

        given(transactionManager.getVoteTransactionByHash(hash)).willReturn(expectedTransaction)

        val actualResponse = webClient.get().uri("$VOTE_TRANSACTION_URL/$hash")
            .exchange()
            .expectStatus().isOk
            .expectBody(VoteTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

}
