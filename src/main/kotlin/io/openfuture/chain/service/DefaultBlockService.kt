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
import io.openfuture.chain.util.HashUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    override fun count(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getAll(pageRequest: Pageable): Page<Block> = repository.findAll(pageRequest)

    @Transactional(readOnly = true)
    override fun getLast(): Block = repository.findFirstByOrderByOrderNumberDesc()
            ?: throw NotFoundException("Last block not exist!")

    @Transactional
    override fun add(block: BlockDto): Block {
        if (!this.isValid(block)) {
            throw ValidationException("Block is not valid!")
        }

        val persistBlock = repository.save(Block.of(block))
        val transactions = block.blockData.merkleHash.transactions.map { transactionService.save(persistBlock, it) }
        persistBlock.transactions.addAll(transactions)
        return persistBlock
    }

    override fun createNext(privateKey: String, publicKey: String, difficulty: Int,
                            transactions: MutableList<TransactionDto>): BlockDto {
        val previousBlock = this.getLast()
        val nextTimeStamp = nodeClock.networkTime()
        val nextOrderNumber = previousBlock.orderNumber + 1
        val previousHash = previousBlock.hash
        val merkleHash = generateMerkleHash(transactions)
        val blockData = BlockData(nextTimeStamp, nextOrderNumber, previousHash, merkleHash)
        val blockHash = generateBlockHash(difficulty, blockData)
        val signature = generateSignature(privateKey,blockData, blockHash)

        return BlockDto(blockData, blockHash, publicKey, signature)
    }

    override fun isValid(block: BlockDto): Boolean {
        if (!isValidHash(block.blockData, block.blockHash)) {
            return false
        }

        if (!isValidSignature(block.nodePublicKey, block.nodeSignature, block.blockData, block.blockHash)) {
            return false
        }

        return true
    }

    private fun isValidHash(blockData: BlockData, blockHash: BlockHash): Boolean {
        val data = getHashData(blockData, blockHash.nonce)
        return blockHash.hash == HashUtils.generateHash(data)
    }

    private fun isValidSignature(publicKey: String, signature: String, blockData: BlockData,
                                 blockHash: BlockHash): Boolean {
        val data = getSignatureData(blockData, blockHash)
        return HashUtils.validateSignature(publicKey, signature, data)
    }

    // todo temp, need discuss it
    override fun createGenesis(): BlockDto {
        return BlockDto(generateGenesisData(), generateGenesisBlockHash(), generateGenesisPublicKey(),
                generateGenesisSignature())
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
        var currentHash = HashUtils.generateHash(getHashData(blockData, currentNonce))
        val target = HashUtils.getDificultyString(difficulty)
        while (currentHash.substring(0, difficulty) != target) {
            currentNonce++
            currentHash = HashUtils.generateHash(getHashData(blockData, currentNonce))
        }
        return BlockHash(currentNonce, currentHash)
    }

    private fun generateSignature(privateKey: String, blockData: BlockData, blockHash: BlockHash): String {
        val data = getSignatureData(blockData, blockHash)
        return HashUtils.generateSignature(privateKey, data)
    }

    //todo temp solution; need discuss it..
    private fun generateGenesisData(): BlockData {
        return BlockData(0, 0, "previousHash", MerkleHash("merkleHash", listOf()))
    }

    //todo temp solution; need discuss it..
    private fun generateGenesisBlockHash(): BlockHash {
        val hash = HashUtils.generateHash("genesisHash".toByteArray())
        return BlockHash(0, hash)
    }

    //todo temp solution; need discuss it..
    private fun generateGenesisPublicKey(): String {
        return HashUtils.generateHash("publicKey".toByteArray())
    }

    //todo temp solution; need discuss it..
    private fun generateGenesisSignature(): String {
        return HashUtils.generateHash("signature".toByteArray())
    }

    private fun getHashData(blockData: BlockData, nonce: Long): ByteArray {
        return dataBuilder(blockData, nonce)
    }

    private fun getSignatureData(blockData: BlockData, blockHash: BlockHash): ByteArray {
        return dataBuilder(blockData, blockHash.nonce, blockHash.hash)
    }

    private fun dataBuilder(blockData: BlockData, nonce: Long): ByteArray {
        return dataBuilder(blockData, nonce, null)
    }

    private fun dataBuilder(blockData: BlockData, nonce: Long, hash: String?): ByteArray {
        val builder = StringBuilder()
        builder.append(blockData.timestamp)
        builder.append(blockData.orderNumber)
        builder.append(blockData.previousHash)
        builder.append(blockData.merkleHash.hash)
        builder.append(nonce)
        if (null != hash) {
            builder.append(hash)
        }
        return builder.toString().toByteArray()
    }

}