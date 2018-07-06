package io.openfuture.chain.service

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.crypto.util.HashUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

@Service
class DefaultBlockService(
        private val repository: BlockRepository,
        private val transactionService: TransactionService,
        private val nodeClock: NodeClock
) : BlockService {

    @Transactional(readOnly = true)
    override fun chainSize(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getLast(): Block = repository.findFirstByOrderByOrderNumberDesc()
            ?: throw NotFoundException("Last block not exist!")

    @Transactional
    override fun add(block: BlockDto): Block {
        // todo need to add validation
        val persistBlock = repository.save(Block.of(block))
        val transactions = block.blockData.merkleHash.transactions.map { transactionService.addToBlock(it.hash, persistBlock) }
        persistBlock.transactions.addAll(transactions)
        return persistBlock
    }

    @Deprecated("temp solution")
    override fun create(privateKey: String, publicKey: String, difficulty: Int): BlockDto {
        val previousBlock = this.getLast()
        val networkTime = nodeClock.networkTime()
        val nextOrderNumber = previousBlock.orderNumber + 1
        val previousHash = previousBlock.hash
        val transactions = transactionService.getAllPending().map { TransactionDto(it) }
        val merkleHash = generateMerkleHash(transactions)
        val blockData = BlockData(nextOrderNumber, previousHash, merkleHash)
        val signature = HashUtils.generateHash(privateKey.toByteArray() + blockData.getByteData())
        return BlockDto.of(networkTime, blockData, publicKey, signature)
    }

    private fun generateMerkleHash(transactions: List<TransactionDto>): MerkleHash {
        if (CollectionUtils.isEmpty(transactions)) {
            throw IllegalArgumentException("Transactions must not be empty!")
        }
        return MerkleHash(calculateThreeHash(transactions.map { it -> it.hash }.toMutableList()), transactions)
    }

    private fun calculateThreeHash(elements: MutableList<String>): String {
        if (1 == elements.size) {
            return HashUtils.generateHash(elements.first().toByteArray())
        }

        if (elements.size % 2 != 0) {
            elements.add(elements.last())
        }

        val newHashElements = mutableListOf<String>()
        for (i in elements.indices step 2) {
            newHashElements.add(HashUtils.generateHash((elements[i] + elements[i + 1]).toByteArray()))
        }
        return calculateThreeHash(newHashElements)
    }

}