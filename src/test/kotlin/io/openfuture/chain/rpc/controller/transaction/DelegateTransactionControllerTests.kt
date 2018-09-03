package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
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

    companion object {
        private const val DELEGATE_TRANSACTION_URL = "/rpc/transactions/delegate"
    }


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val transactionRequest = DelegateTransactionRequest(1L, 1L, "hash", "senderAddress", "senderPublicKey", "senderSignature",
            "delegateKey", "host", 1, 1)
        val header = TransactionHeader(1L, 1L, "senderAddress")
        val footer = TransactionFooter("senderPublicKey", "senderSignature", "hash")
        val payload = DelegateTransactionPayload("delegateKey", "host", 1, 1)
        val unconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(header, footer, payload)
        val expectedResponse = DelegateTransactionResponse(unconfirmedDelegateTransaction)

        given(service.add(transactionRequest)).willReturn(unconfirmedDelegateTransaction)

        val actualResponse = webClient.post().uri(DELEGATE_TRANSACTION_URL)
            .body(Mono.just(transactionRequest), DelegateTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(DelegateTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

    @Test
    fun getTransactionByHashShouldReturnTransaction() {
        val hash = "hash"
        val mainBlock = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey", MainBlockPayload("merkleHash")).apply { id = 1 }
        val header = TransactionHeader(1L, 1L, "senderAddress")
        val footer = TransactionFooter("senderPublicKey", "senderSignature", "hash")
        val payload = DelegateTransactionPayload("delegateKey", "delegateHost", 8080, 1)
        val delegateTransaction = DelegateTransaction(header, footer, mainBlock, payload).apply { id = 1 }
        val expectedResponse = DelegateTransactionResponse(delegateTransaction)

        given(service.getByHash(hash)).willReturn(delegateTransaction)

        val actualResponse = webClient.get().uri("$DELEGATE_TRANSACTION_URL/$hash")
            .exchange()
            .expectStatus().isOk
            .expectBody(DelegateTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

}

