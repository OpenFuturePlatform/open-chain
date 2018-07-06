package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mock

class DefaultTransactionServiceTests : ServiceTests() {

    @Mock private lateinit var repository: TransactionRepository
    @Mock private lateinit var walletService: DefaultWalletService
    @Mock private lateinit var blockService: DefaultBlockService

    private lateinit var service: TransactionService


    @Before
    fun setUp() {
        service = DefaultTransactionService(repository, walletService, blockService)
    }

    @Test
    fun saveShouldReturnSavedTransactionTest() {
        val request = TransactionRequest(1, 1, 1L, "recipientKey", "senderKey", "signature", "from", "to")
        val block = Block(1, 1L, 1L, "previousHash", "hash", "merkleHash", "nodeKey", "nodeSignature")

        given(blockService.get(request.blockId)).willReturn(block)
        given(repository.save(any(Transaction::class.java))).will { invocation -> invocation.arguments[0] }

        val actualTransaction = service.save(request)

        verify(walletService).updateByTransaction(actualTransaction)

        assertThat(actualTransaction.block).isEqualTo(block)
        assertThat(actualTransaction.hash).isEqualTo(request.hash)
        assertThat(actualTransaction.senderKey).isEqualTo(request.senderKey)
        assertThat(actualTransaction.recipientKey).isEqualTo(request.recipientKey)
    }

}