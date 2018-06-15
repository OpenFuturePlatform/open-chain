package io.openfuture.chain.domain.transaction

import io.openfuture.chain.util.HashUtils

/**
 * @author Homza Pavel
 */
class TransactionRequest(
    val blockId: Int = 0,
    amount: Int,
    timestamp: Long,
    recipientkey: String,
    senderKey: String,
    signature: String

) : PendingTransactionRequest(amount, timestamp, recipientkey, senderKey, signature) {

    var hash: String = calculateHash()

    private fun calculateHash(): String {
        val builder = StringBuilder()
        builder.append(this.amount)
        builder.append(this.timestamp)
        builder.append(this.recipientkey)
        builder.append(this.senderKey)
        builder.append(this.signature)
        return HashUtils.generateHash(builder.toString().toByteArray())
    }
}