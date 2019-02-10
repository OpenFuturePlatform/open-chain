package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.config.any
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.service.TransactionManager
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
    private lateinit var transactionManager: TransactionManager

    companion object {
        private const val DELEGATE_TRANSACTION_URL = "/rpc/transactions/delegate"
        private const val WALLET_ADDRESS = "0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb"
    }


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val request = DelegateTransactionRequest(1L, 1L, WALLET_ADDRESS, 1L,
            "delegateKey", "hash", "senderSignature", "senderPublicKey")
        val unconfirmedDelegateTransaction = UnconfirmedDelegateTransaction.of(request)
        val expectedResponse = DelegateTransactionResponse(unconfirmedDelegateTransaction)

        given(transactionManager.add(any(UnconfirmedDelegateTransaction::class.java))).willReturn(unconfirmedDelegateTransaction)

        val actualResponse = webClient.post().uri(DELEGATE_TRANSACTION_URL)
            .body(Mono.just(request), DelegateTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(DelegateTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

    @Test
    fun getTransactionByHashShouldReturnTransaction() {
        val hash = "hash"
        val mainBlock = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey",
            MainBlockPayload("merkleHash", "stateHash", "receiptHash")).apply { id = 1 }
        val payload = DelegateTransactionPayload("delegateKey", 1)
        val delegateTransaction = DelegateTransaction(1L, 1L, WALLET_ADDRESS, "hash", "senderSignature",
            "senderPublicKey", payload, mainBlock).apply { id = 1 }
        val expectedResponse = DelegateTransactionResponse(delegateTransaction)

        given(transactionManager.getDelegateTransactionByHash(hash)).willReturn(delegateTransaction)

        val actualResponse = webClient.get().uri("$DELEGATE_TRANSACTION_URL/$hash")
            .exchange()
            .expectStatus().isOk
            .expectBody(DelegateTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

}

