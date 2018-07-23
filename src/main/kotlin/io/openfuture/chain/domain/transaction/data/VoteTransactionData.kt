package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.entity.dictionary.VoteType

class VoteTransactionData(
    amount: Long,
    recipientAddress: String,
    senderAddress: String,
    var voteType: VoteType,
    var delegateKey: String
) : BaseTransactionData(amount, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        builder.append(voteType)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

}