package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.config.any
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.transaction.request.TransactionPageRequest
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

    @MockBean private lateinit var transactionManager: TransactionManager
    @MockBean private lateinit var receiptService: ReceiptService

    companion object {
        private const val TRANSFER_TRANSACTION_URL = "/rpc/transactions/transfer"
        private const val WALLET_ADDRESS = "0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb"
    }


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val request = TransferTransactionRequest(1L, 1L, "hash", WALLET_ADDRESS,
            "senderSignature", "senderPublicKey", 1, WALLET_ADDRESS, "recipientAddress")
        val unconfirmedTransferTransaction = UnconfirmedTransferTransaction.of(request)
        val expectedResponse = TransferTransactionResponse(unconfirmedTransferTransaction)

        given(transactionManager.add(any(UnconfirmedTransferTransaction::class.java))).willReturn(unconfirmedTransferTransaction)

        val actualResponse = webClient.post().uri(TRANSFER_TRANSACTION_URL)
            .body(Mono.just(request), TransferTransactionRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(TransferTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

    @Test
    fun getAllShouldReturnTransferTransactionsListTest() {
        val pageTransferTransactions = PageImpl(listOf(createTransferTransaction()))
        val expectedPageResponse = PageResponse(pageTransferTransactions)

        given(transactionManager.getAllTransferTransactions(TransactionPageRequest())).willReturn(pageTransferTransactions)
        given(receiptService.getByTransactionHash("hash")).willReturn(createReceipt())

        val actualPageResponse = webClient.get().uri(TRANSFER_TRANSACTION_URL)
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat(((actualPageResponse.list.first() as HashMap<*, *>)["senderAddress"]))
            .isEqualTo(expectedPageResponse.list.first().senderAddress)
        assertThat((actualPageResponse.list.first() as LinkedHashMap<*, *>)["senderPublicKey"])
            .isEqualTo(expectedPageResponse.list.first().publicKey)
    }

    @Test
    fun getTransactionsByAddressShouldReturnTransferTransactionsListTest() {
        val pageTransferTransactions = PageImpl(listOf(createTransferTransaction()))
        val expectedPageResponse = PageResponse(pageTransferTransactions)

        given(transactionManager.getAllTransferTransactionsByAddress(WALLET_ADDRESS, TransactionPageRequest()))
            .willReturn(pageTransferTransactions)
        given(receiptService.getByTransactionHash("hash")).willReturn(createReceipt())

        val actualTransferTransactions = webClient.get().uri("$TRANSFER_TRANSACTION_URL/address/$WALLET_ADDRESS")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(((actualTransferTransactions.list.first() as HashMap<*, *>))["senderAddress"])
            .isEqualTo(expectedPageResponse.list.first().senderAddress)
        assertThat((actualTransferTransactions.list.first() as LinkedHashMap<*, *>)["senderPublicKey"])
            .isEqualTo(expectedPageResponse.list.first().publicKey)
    }

    @Test
    fun getTransactionByHashShouldReturnTransactionWithCurrentHash() {
        val hash = "hash"
        val transferTransaction = createTransferTransaction()
        val expectedResponse = TransferTransactionResponse(transferTransaction, createReceipt())

        given(transactionManager.getTransferTransactionByHash(hash)).willReturn(transferTransaction)
        given(receiptService.getByTransactionHash(hash)).willReturn(createReceipt())

        val actualResponse = webClient.get().uri("$TRANSFER_TRANSACTION_URL/$hash")
            .exchange()
            .expectStatus().isOk
            .expectBody(TransferTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

    private fun getReceiptResults(): List<ReceiptResult> = listOf(ReceiptResult("a", "b", 10))

    private fun createReceipt(): Receipt = Receipt("hash", Receipt.generateResult(getReceiptResults()))

    private fun createTransferTransaction(): TransferTransaction {
        val mainBlock = MainBlock(1, 1, "previousHash", "hash", "signature",
            "publicKey", MainBlockPayload("merkleHash", "stateHash", "receiptHash")).apply { id = 1 }
        val payload = TransferTransactionPayload(1, WALLET_ADDRESS)

        return TransferTransaction(1, 1, WALLET_ADDRESS, "hash", "senderSignature", "senderPublicKey", payload,
            mainBlock).apply { id = 1 }
    }

}
