package io.openfuture.chain.service

import io.openfuture.chain.component.NodeClock
import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.exception.ValidationException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.util.BlockUtils
import io.openfuture.chain.util.HashUtils
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
        if (!this.isValid(block)) {
            throw ValidationException("Block is not valid!")
        }

        val persistBlock = repository.save(Block.of(block))
        val transactions = block.blockData.merkleHash.transactions.map { transactionService.addToBlock(it.hash, persistBlock) }
        persistBlock.transactions.addAll(transactions)
        return persistBlock
    }

    override fun create(privateKey: String, publicKey: String, difficulty: Int,
                        transactions: List<TransactionDto>): BlockDto {
        val previousBlock = this.getLast()
        val nextTimeStamp = nodeClock.networkTime()
        val nextOrderNumber = previousBlock.orderNumber + 1
        val previousHash = previousBlock.hash
        val merkleHash = generateMerkleHash(transactions)
        val blockData = BlockData(nextTimeStamp, nextOrderNumber, previousHash, merkleHash)
        val blockHash = generateBlockHash(difficulty, blockData)
        val signature = BlockUtils.generateSignature(privateKey, blockData, blockHash)

        return BlockDto(blockData, blockHash, publicKey, signature)
    }

    override fun isValid(block: BlockDto): Boolean {
        if (!BlockUtils.isValidHash(block.blockData, block.blockHash)) {
            return false
        }

        if (!BlockUtils.isValidSignature(block.nodePublicKey, block.nodeSignature, block.blockData, block.blockHash)) {
            return false
        }

        return true
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

    // -- mining block process
    private fun generateBlockHash(difficulty: Int, blockData: BlockData): BlockHash {
        var currentNonce = 0L
        var currentHash = BlockUtils.generateHash(blockData, currentNonce)
        val target = HashUtils.getDificultyString(difficulty)
        while (currentHash.substring(0, difficulty) != target) {
            currentNonce++
            currentHash = BlockUtils.generateHash(blockData, currentNonce)
        }
        return BlockHash(currentNonce, currentHash)
    }

}