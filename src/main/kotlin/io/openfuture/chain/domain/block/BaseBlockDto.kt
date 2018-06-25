package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.transaction.TransactionDto

open class BaseBlockDto(
        val timestamp: Long,
        val orderNumber: Long,
        val previousHash: String,
        val transactions: MutableList<TransactionDto>
) {

    protected fun getHashData(merkleHash: String, nonce: Long): ByteArray {
        return dataBuilder(this.timestamp, this.orderNumber, this.previousHash, merkleHash, nonce)
    }

    protected fun getSignatureData(merkleHash: String, nonce: Long, hash: String): ByteArray {
        return dataBuilder(this.timestamp, this.orderNumber, this.previousHash, merkleHash, nonce, hash)
    }

    private fun dataBuilder(timestamp: Long, orderNumber: Long, previousHash: String, merkleHash: String,
                            nonce: Long): ByteArray {
        return dataBuilder(timestamp, orderNumber, previousHash, merkleHash, nonce, null)
    }

    private fun dataBuilder(timestamp: Long, orderNumber: Long, previousHash: String, merkleHash: String,
                            nonce: Long, hash: String?): ByteArray {
        val builder = StringBuilder()
        builder.append(timestamp)
        builder.append(orderNumber)
        builder.append(previousHash)
        builder.append(merkleHash)
        builder.append(nonce)
        if (null != hash) {
            builder.append(hash)
        }
        return builder.toString().toByteArray()
    }

}