package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.payload.TransactionPayload

data class TransactionData(
        val amount: Long,
        val recipientKey: String,
        val senderKey: String,
        val senderSignature: String,
        var payload: TransactionPayload?
) {

    fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientKey)
        builder.append(senderKey)
        builder.append(senderSignature)
        builder.append(payload)
        return builder.toString().toByteArray()
    }

}