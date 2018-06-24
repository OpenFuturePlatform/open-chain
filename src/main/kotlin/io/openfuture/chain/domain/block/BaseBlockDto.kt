package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.transaction.TransactionDto

open class BaseBlockDto(
        val timestamp: Long,
        val orderNumber: Long,
        val previousHash: String,
        val transactions: List<TransactionDto>
) {

    protected fun getHashData(timestamp: Long, orderNumber: Long, previousHash: String,
                            merkleHash: String, nonce: Long): ByteArray {
        val builder = dataBuilder(timestamp, orderNumber, previousHash, merkleHash, nonce)
        return builder.toString().toByteArray()
    }

    protected fun getSignatureData(timestamp: Long, orderNumber: Long, previousHash: String,
                                 merkleHash: String, nonce: Long, hash: String): ByteArray {
        val builder = dataBuilder(timestamp, orderNumber, previousHash, merkleHash, nonce)
        builder.append(hash)
        return builder.toString().toByteArray()
    }

    private fun dataBuilder(timestamp: Long, orderNumber: Long, previousHash: String, merkleHash: String,
                            nonce: Long): StringBuilder {
        val builder = StringBuilder()
        builder.append(timestamp)
        builder.append(orderNumber)
        builder.append(previousHash)
        builder.append(merkleHash)
        builder.append(nonce)
        return builder
    }

}