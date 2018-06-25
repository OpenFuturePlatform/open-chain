package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.util.HashUtils
import org.springframework.util.CollectionUtils

class BlockDto(
         tmestamp: Long,
         orderNumber: Long,
         previousHash: String,
         transactions: MutableList<TransactionDto>
): BaseBlockDto(tmestamp, orderNumber, previousHash, transactions) {

    fun mineBlock(privateKey: String, publicKey: String, difficulty: Int): MinedBlockDto {
        val merkleHash = generateMerkleHash(transactions)
        val blockHash = generateBlockHash(difficulty, merkleHash)
        val signature = generateSignature(privateKey, merkleHash, blockHash.nonce, blockHash.hash)
        return MinedBlockDto(this.timestamp, this.orderNumber, this.previousHash, this.transactions, merkleHash,
                blockHash, publicKey, signature)
    }

    private fun generateMerkleHash(transactions: List<TransactionDto>): String {
        if (CollectionUtils.isEmpty(transactions)) {
            throw IllegalArgumentException("Transactions must not be empty!")
        }
        return calculateThreeHash(transactions.map { it -> it.hash }.toMutableList())
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
    private fun generateBlockHash(difficulty: Int, merkleHash: String): BlockHash {
        var currentNonce = 0L
        var currentHash = HashUtils.generateHash(getHashData(merkleHash, currentNonce))
        val target = HashUtils.getDificultyString(difficulty)
        while (currentHash.substring(0, difficulty) != target) {
            currentNonce++
            currentHash = HashUtils.generateHash(getHashData(merkleHash, currentNonce))
        }
        return BlockHash(currentNonce, currentHash)
    }

    private fun generateSignature(privateKey: String, merkleHash: String, nonce: Long, hash: String): String {
        val data = getSignatureData(merkleHash, nonce, hash)
        return HashUtils.generateSignature(privateKey, data)
    }

}
