package io.openfuture.chain.domain.transaction

import io.openfuture.chain.crypto.util.HashUtils

class TransactionRequest(
    var blockId: Int,
    amount: Int,
    timestamp: Long,
    recipientKey: String,
    senderKey: String,
    signature: String,
    senderAddress: String,
    recipientAddress: String

) : PendingTransactionRequest(amount, timestamp, recipientKey, senderKey, signature, senderAddress , recipientAddress) {

    var hash: String = calculateHash()

    private fun calculateHash(): String {
        val builder = StringBuilder()
        builder.append(this.amount)
        builder.append(this.timestamp)
        builder.append(this.recipientKey)
        builder.append(this.senderKey)
        builder.append(this.signature)
        builder.append(this.senderAddress)
        builder.append(this.recipientAddress)
        return HashUtils.generateHash(builder.toString().toByteArray())
    }
}