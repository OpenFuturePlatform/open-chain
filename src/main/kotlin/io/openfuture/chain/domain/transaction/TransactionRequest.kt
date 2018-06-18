package io.openfuture.chain.domain.transaction

import io.openfuture.chain.util.HashUtils

class TransactionRequest(
    val blockId: Int,
    amount: Int,
    timestamp: Long,
    recipientKey: String,
    senderKey: String,
    signature: String

) : PendingTransactionRequest(amount, timestamp, recipientKey, senderKey, signature) {

    var hash: String = calculateHash()

    private fun calculateHash(): String {
        val builder = StringBuilder()
        builder.append(this.amount)
        builder.append(this.timestamp)
        builder.append(this.recipientKey)
        builder.append(this.senderKey)
        builder.append(this.signature)
        return HashUtils.generateHash(builder.toString().toByteArray())
    }
}