package io.openfuture.chain.service

import io.openfuture.chain.component.NodeClock
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.BlockRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mock


internal class DefaultBlockServiceTest: ServiceTests() {

    @Mock private lateinit var repository: BlockRepository
    @Mock private lateinit var transactionService: TransactionService
    @Mock private lateinit var nodeClock: NodeClock

    private lateinit var service: BlockService


    @Before
    fun setUp() {
        service = DefaultBlockService(repository, transactionService, nodeClock)
    }

    @Test
    fun chainSize() {
        val expectedCount = 1L
        given(repository.count()).willReturn(expectedCount)
        val actualCount = service.chainSize()
        assertThat(actualCount).isEqualTo(expectedCount)
    }

    @Test
    fun getLast() {
        val expectedBlock = createBlock()
        given(repository.findFirstByOrderByOrderNumberDesc()).willReturn(expectedBlock)
        val actualBlock = service.getLast()
        assertThat(actualBlock).isEqualTo(expectedBlock)
    }

    @Test
    fun add() {
        val transaction = createTransactionDto()
        val block = createNextBlockDto(mutableListOf(transaction))
        val expectedBlock = Block.of(block)
        val expectedTransaction = Transaction.of(expectedBlock, transaction)
        expectedBlock.transactions = mutableListOf(expectedTransaction)

        given(repository.save(any(Block::class.java))).willReturn(expectedBlock)
        given(transactionService.save(expectedBlock, transaction)).willReturn(expectedTransaction)

        val actualBlock = service.add(block)
        assertThat(actualBlock).isEqualTo(expectedBlock)
    }

    private fun createNextBlockDto(transactions: MutableList<TransactionDto>): BlockDto {
        val previousBlock = createBlockDto(mutableListOf())
        given(repository.findFirstByOrderByOrderNumberDesc()).willReturn(Block.of(previousBlock))
        return service.create("privateKey", "publicKey", 1, transactions)
    }

    private fun createBlockDto(transactions: MutableList<TransactionDto>): BlockDto = BlockDto(BlockData(0,
            0, "previousHash", MerkleHash("merkleHash", transactions)),
            BlockHash(0, "hash"), "nodePublicKey", "nodeSignature")

    private fun createTransactionDto(): TransactionDto = TransactionDto(0, 0,
            "recipientKey", "senderKey", "signature")

    private fun createBlock(): Block = Block(1, 0, "previousHash",
            "merkleHash", 0, "hash", "nodeKey", "nodeSignature", mutableListOf())

}