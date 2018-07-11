package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.entity.dictionary.VoteType

class VoteTransactionData(
    amount: Long,
    recipientKey: String,
    senderKey: String,
    senderSignature: String,
    var voteType: VoteType,
    var delegateKey: String,
    var weight: Int
) : BaseTransactionData(amount, recipientKey, senderKey, senderSignature) {

    override fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientKey)
        builder.append(senderKey)
        builder.append(senderSignature)
        builder.append(voteType)
        builder.append(delegateKey)
        builder.append(weight)
        return builder.toString().toByteArray()
    }

}