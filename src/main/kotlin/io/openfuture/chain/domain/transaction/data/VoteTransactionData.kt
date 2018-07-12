package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.domain.delegate.DelegateNetworkInfo
import io.openfuture.chain.entity.dictionary.VoteType

class VoteTransactionData(
    amount: Long,
    recipientKey: String,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    var voteType: VoteType,
    var delegateInfo: DelegateNetworkInfo
) : BaseTransactionData(amount, recipientKey, recipientAddress, senderKey, senderAddress, senderSignature) {

    override fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientKey)
        builder.append(recipientAddress)
        builder.append(senderKey)
        builder.append(senderAddress)
        builder.append(senderSignature)
        builder.append(voteType)
        builder.append(delegateInfo)
        return builder.toString().toByteArray()
    }

}