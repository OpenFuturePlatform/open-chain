package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.util.HashUtils
import org.springframework.util.CollectionUtils


class BlockRequest {

    var timestamp: Long? = null
    var orderNumber: Int? = null
    var nonce: Long? = null
    var previousHash: String? = null
    var hash: String? = null
    var merkleHash: String? = null
    var nodePublicKey: String? = null
    var nodeSignature: String? = null
    var transactions: List<TransactionRequest> = listOf()

    constructor(orderNumber: Int, nonce: Long, previousHash: String, hash: String, merkleHash: String,
                nodeKey: String, nodeSignature: String, transactions: List<TransactionRequest>) {
        this.orderNumber = orderNumber
        this.nonce = nonce
        this.previousHash = previousHash
        this.hash = hash
        this.merkleHash = merkleHash
        this.nodePublicKey = nodeKey
        this.nodeSignature = nodeSignature
        this.transactions = transactions
    }

    constructor(difficulty: Int, timestamp: Long, orderNumber: Int, previousHash: String, nodePrivateKey: String,
                nodePublicKey: String, transactions: List<TransactionRequest>) {
        val merkleHash = generateMerkleHash(transactions)
        val blockHash = generateBlockHash(difficulty, timestamp, orderNumber, previousHash, merkleHash, nodePublicKey)
        val signature = generateSignature(nodePrivateKey, blockHash.nonce, timestamp, orderNumber, previousHash, merkleHash, nodePublicKey)

        this.timestamp = timestamp
        this.orderNumber = orderNumber
        this.previousHash = previousHash
        this.nodePublicKey = nodePublicKey
        this.transactions = transactions
        this.merkleHash = merkleHash
        this.nonce = blockHash.nonce
        this.hash = blockHash.hash
        this.nodeSignature = signature
    }

    fun isValid(): Boolean {
        if (!isValidSignature()) {
            return false
        }

        if (!isValidHash()) {
            return false
        }

        return true
    }

    private fun isValidSignature(): Boolean {
        val data = getByteData(this.nonce!!, this.timestamp!!, this.orderNumber!!, this.previousHash!!,
                this.merkleHash!!, this.nodePublicKey!!)
        return HashUtils.validateSignature(this.nodePublicKey!!, this.nodeSignature!!, data)
    }

    private fun isValidHash(): Boolean {
        val data = getByteData(this.nonce!!, this.timestamp!!, this.orderNumber!!, this.previousHash!!,
                this.merkleHash!!, this.nodePublicKey!!)
        return this.hash == HashUtils.generateHash(data)
    }

    // -- mining block process
    private fun generateBlockHash(difficulty: Int, timestamp: Long, orderNumber: Int, previousHash: String,
                                  merkleHash: String, nodePublicKey: String): BlockHash {
        var currentNonce = 0L
        var currentHash = HashUtils.generateHash(getByteData(currentNonce, timestamp, orderNumber, previousHash,
                merkleHash, nodePublicKey))
        val target = HashUtils.getDificultyString(difficulty)
        while (currentHash.substring(0, difficulty) != target) {
            currentNonce++
            currentHash = HashUtils.generateHash(getByteData(currentNonce, timestamp, orderNumber, previousHash,
                    merkleHash, nodePublicKey))
        }
        return BlockHash(currentNonce, currentHash)
    }

    private fun generateMerkleHash(transactions: List<TransactionRequest>): String {
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
            newHashElements.add(HashUtils.generateHash((elements[i] + elements[i+1]).toByteArray()))
        }
        return calculateThreeHash(newHashElements)
    }

    private fun generateSignature(privateKey: String, nonce: Long, timestamp: Long, orderNumber: Int,
                                  previousHash: String, merkleHash: String, nodePublicKey: String): String {
        val data = getByteData(nonce, timestamp, orderNumber, previousHash, merkleHash, nodePublicKey)
        return HashUtils.generateSignature(privateKey, data)
    }

    private fun getByteData(nonce: Long, timestamp: Long, orderNumber: Int, previousHash: String,
                            merkleHash: String, nodePublicKey: String): ByteArray {
        val builder = StringBuilder()
        builder.append(nonce)
        builder.append(timestamp)
        builder.append(orderNumber)
        builder.append(previousHash)
        builder.append(merkleHash)
        builder.append(nodePublicKey)
        return builder.toString().toByteArray()
    }

}
