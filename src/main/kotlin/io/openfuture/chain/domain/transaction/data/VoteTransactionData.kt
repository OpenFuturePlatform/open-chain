package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.domain.transaction.vote.VoteDto

class VoteTransactionData(
        amount: Long,
        recipientKey: String,
        senderKey: String,
        senderSignature: String,
        var votes: MutableList<VoteDto>
) : TransactionData(amount, recipientKey, senderKey, senderSignature) {

    override fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientKey)
        builder.append(senderKey)
        builder.append(senderSignature)
        builder.append(votes)
        return builder.toString().toByteArray()
    }

}