package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
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

    companion object {
        private const val TRANSFER_TRANSACTION_URL = "/rpc/transactions/transfer"
        private const val WALLET_ADDRESS = "0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb"
    }


    @Test
    fun addTransactionShouldReturnAddedTransaction() {
        val transactionRequest = TransferTransactionRequest(1L, 1L, "hash", WALLET_ADDRESS,
            1, WALLET_ADDRESS, "senderSignature", "recipientAddress")
        val header = TransactionHeader(1L, 1L, WALLET_ADDRESS)
        val footer = TransactionFooter("hash", "senderSignature", "senderPublicKey")
        val payload = TransferTransactionPayload(1L, WALLET_ADDRESS)
        val unconfirmedTransferTransaction = UnconfirmedTransferTransaction(header, footer, payload)
        val expectedResponse = TransferTransactionResponse(unconfirmedTransferTransaction)

        given(service.add(transactionRequest)).willReturn(unconfirmedTransferTransaction)

        val actualResponse = webClient.post().uri(TRANSFER_TRANSACTION_URL)
            .body(Mono.just(transactionRequest), TransferTransactionRequest::class.java)
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

        given(service.getAll(PageRequest())).willReturn(pageTransferTransactions)

        val actualPageResponse = webClient.get().uri(TRANSFER_TRANSACTION_URL)
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualPageResponse.totalCount).isEqualTo(expectedPageResponse.totalCount)
        assertThat(((actualPageResponse.list.first() as HashMap<*, *>)["senderAddress"])).isEqualTo(expectedPageResponse.list.first().header.senderAddress)
        assertThat((actualPageResponse.list.first() as LinkedHashMap<*, *>)["senderPublicKey"]).isEqualTo(expectedPageResponse.list.first().footer.senderPublicKey)
    }

    @Test
    fun getTransactionsByAddressShouldReturnTransferTransactionsListTest() {
        val pageTransferTransactions = PageImpl(listOf(createTransferTransaction()))
        val expectedPageResponse = PageResponse(pageTransferTransactions)

        given(service.getByAddress(WALLET_ADDRESS, PageRequest())).willReturn(pageTransferTransactions)

        val actualTransferTransactions = webClient.get().uri("$TRANSFER_TRANSACTION_URL/address/$WALLET_ADDRESS")
            .exchange()
            .expectStatus().isOk
            .expectBody(PageResponse::class.java)
            .returnResult().responseBody!!

        assertThat(((actualTransferTransactions.list.first() as HashMap<*, *>))["senderAddress"])
            .isEqualTo(expectedPageResponse.list.first().header.senderAddress)
        assertThat((actualTransferTransactions.list.first() as LinkedHashMap<*, *>)["senderPublicKey"])
            .isEqualTo(expectedPageResponse.list.first().footer.senderPublicKey)
    }

    @Test
    fun getTransactionByHashShouldReturnTransactionWithCurrentHash() {
        val hash = "hash"
        val transferTransaction = createTransferTransaction()
        val expectedResponse = TransferTransactionResponse(transferTransaction)

        given(service.getByHash(hash)).willReturn(transferTransaction)

        val actualResponse = webClient.get().uri("$TRANSFER_TRANSACTION_URL/$hash")
            .exchange()
            .expectStatus().isOk
            .expectBody(TransferTransactionResponse::class.java)
            .returnResult().responseBody!!

        assertThat(actualResponse).isEqualToComparingFieldByField(expectedResponse)
    }

    private fun createTransferTransaction(): TransferTransaction {
        val mainBlock = MainBlock(1, 1, "previousHash", "hash", "signature",
            "publicKey", MainBlockPayload("merkleHash")).apply { id = 1 }
        val header = TransactionHeader(1, 1, WALLET_ADDRESS)
        val footer = TransactionFooter("hash", "senderSignature", "senderPublicKey")
        val payload = TransferTransactionPayload(1, WALLET_ADDRESS)
        return TransferTransaction(header, footer, mainBlock, payload).apply { id = 1 }

    }

}
