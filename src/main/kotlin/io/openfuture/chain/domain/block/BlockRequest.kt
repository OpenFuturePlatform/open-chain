package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.util.HashUtils

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
        val blockHash = generateBlockHash(difficulty, timestamp, orderNumber, previousHash, merkleHash)
        val signature = generateSignature(nodePrivateKey, timestamp, orderNumber, previousHash, merkleHash)

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

    //todo need to add logic
    private fun isValidSignature(): Boolean {
        return true
    }

    //todo need to add logic
    private fun isValidHash(): Boolean {
        return true
    }

    // -- mining block process
    private fun generateBlockHash(difficulty: Int, timestamp: Long, orderNumber: Int, previousHash: String,
                                  merkleHash: String): BlockHash {
        var currentNonce = 0L
        var currentHash = HashUtils.generateHash(getByteData(currentNonce, timestamp, orderNumber, previousHash,
                merkleHash))
        val target = HashUtils.getDificultyString(difficulty)
        while (currentHash.substring(0, difficulty) != target) {
            currentNonce++
            currentHash = HashUtils.generateHash(getByteData(currentNonce, timestamp, orderNumber, previousHash,
                    merkleHash))
        }
        return BlockHash(currentNonce, currentHash)
    }

    //todo temp solution, need to implement merkle tree
    private fun generateMerkleHash(transactions: List<TransactionRequest>): String {
        val builder = StringBuilder()
        transactions.forEach { it -> builder.append(it.hash) }
        return HashUtils.generateHash(builder.toString().toByteArray())
    }

    //todo need to add logic
    fun generateSignature(privateKey: String, timestamp: Long, orderNumber: Int, previousHash: String,
                          merkleHash: String): String {
        return HashUtils.generateHash(privateKey.toByteArray())
    }

    private fun getByteData(nonce: Long, timestamp: Long, orderNumber: Int, previousHash: String,
                            merkleHash: String): ByteArray {
        val builder = StringBuilder()
        builder.append(nonce)
        builder.append(timestamp)
        builder.append(orderNumber)
        builder.append(previousHash)
        builder.append(merkleHash)
        return builder.toString().toByteArray()
    }

}
