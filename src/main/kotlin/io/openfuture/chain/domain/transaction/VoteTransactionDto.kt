package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.delegate.DelegateNetworkInfo
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.util.TransactionUtils

class VoteTransactionDto(
    timestamp: Long,
    amount: Long,
    recipientKey: String,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String,
    val voteType: VoteType,
    val delegateInfo: DelegateNetworkInfo
) : BaseTransactionDto(timestamp, amount, recipientKey, recipientAddress, senderKey, senderAddress,
    senderSignature, hash) {

    companion object {
        fun of(networkTime: Long, data: VoteTransactionData) = VoteTransactionDto(
            networkTime,
            data.amount!!,
            data.recipientKey!!,
            data.recipientAddress!!,
            data.senderKey!!,
            data.senderAddress!!,
            data.senderSignature!!,
            TransactionUtils.calculateHash(networkTime, data),
            data.voteType,
            data.delegateInfo
        )
    }

}