package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.util.HashUtils

class MinedBlockDto(
        timestamp: Long,
        orderNumber: Long,
        previousHash: String,
        transactions: MutableList<TransactionDto>,
        var merkleHash: String,
        var blockHash: BlockHash,
        val nodePublicKey: String,
        var nodeSignature: String
): BaseBlockDto(timestamp, orderNumber, previousHash, transactions) {

    fun isValid(): Boolean {

        if (!isValidHash()) {
            return false
        }

        if (!isValidSignature()) {
            return false
        }

        return true
    }

    private fun isValidHash(): Boolean {
        val data = getHashData(this.merkleHash, this.blockHash.nonce)
        return this.blockHash.hash == HashUtils.generateHash(data)
    }

    private fun isValidSignature(): Boolean {
        val data = getSignatureData(this.merkleHash, this.blockHash.nonce, this.blockHash.hash)
        return HashUtils.validateSignature(this.nodePublicKey, this.nodeSignature, data)
    }

}