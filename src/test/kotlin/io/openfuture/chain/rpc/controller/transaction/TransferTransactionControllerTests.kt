package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.TransferTransactionResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import reactor.core.publisher.Mono

@WebFluxTest(TransferTransactionController::class)
class TransferTransactionControllerTests : ControllerTests() {

    @MockBean
    private lateinit var service: TransferTransactionService


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val transactionRequest = TransferTransactionRequest(1L, 1L, "senderAddress",
            1, "recipientAddress", "senderSignature", "recipientAddress")
        val unconfirmedTransferTransaction = UnconfirmedTransferTransaction(1L, 1L, "senderAddress",
            "hash", "senderSignature", "senderPublicKey", TransferTransactionPayload(1L, "delegateKey"))
        val expectedResponse = TransferTransactionResponse(unconfirmedTransferTransaction)

        given(service.add(transactionRequest)).willReturn(unconfirmedTransferTransaction)

        val actualResponse = webClient.post().uri("/rpc/transactions/transfer")
            .body(Mono.just(transactionRequest), TransferTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(TransferTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

    @Test
    fun getAllShouldReturnTransferTransactionsListTest() {
        val mainBlock = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey", MainBlockPayload("merkleHash"))
        val pageTransferTransactions = PageImpl(listOf(TransferTransaction(1, 1, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", mainBlock, TransferTransactionPayload(1, "recipientAddress"))))
        val expectedPageResponse = PageResponse(pageTransferTransactions)

        given(service.getAll(PageRequest())).willReturn(pageTransferTransactions)

        val actualPageResponse = webClient.get().uri("/rpc/transactions/transfer")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["senderAddress"]).isEqualTo(expectedPageResponse.list.first().senderAddress)
        assertThat((actualPageResponse.list[0] as LinkedHashMap<*, *>)["senderPublicKey"]).isEqualTo(expectedPageResponse.list.first().senderPublicKey)
    }

    @Test
    fun getTransactionsByAddressShouldReturnTransferTransactionsListTest() {
        val address = "address"
        val mainBlock = MainBlock(1, 1, "previousHash", "hash", "signature", "publicKey", MainBlockPayload("merkleHash"))
        val expectedTransferTransactions = listOf(TransferTransaction(1, 1, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", mainBlock, TransferTransactionPayload(1, "recipientAddress")))

        given(service.getByAddress(address)).willReturn(expectedTransferTransactions)

        val actualTransferTransactions = webClient.get().uri("/rpc/transactions/transfer/$address")
            .exchange()
            .expectStatus().isOk
            .expectBody(List::class.java)
            .returnResult().responseBody!!

        assertThat((actualTransferTransactions.first() as LinkedHashMap<*, *>)["senderAddress"])
            .isEqualTo(expectedTransferTransactions.first().senderAddress)
        assertThat((actualTransferTransactions.first()  as LinkedHashMap<*, *>)["senderPublicKey"])
            .isEqualTo(expectedTransferTransactions.first().senderPublicKey)
    }

}
