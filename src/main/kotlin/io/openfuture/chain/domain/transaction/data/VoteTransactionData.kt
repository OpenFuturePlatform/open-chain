package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.entity.dictionary.VoteType

class VoteTransactionData(
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    var voteType: VoteType,
    var delegateKey: String
) : BaseTransactionData(amount, fee, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(fee)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        builder.append(voteType)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

}