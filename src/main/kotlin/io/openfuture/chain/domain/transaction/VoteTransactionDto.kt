package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.delegate.DelegateInfo
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction

class VoteTransactionDto(
    timestamp: Long,
    amount: Double,
    fee: Double,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String,

    val voteType: VoteType,
    val delegateInfo: DelegateInfo
) : BaseTransactionDto(timestamp, amount, fee, recipientAddress, senderKey, senderAddress, senderSignature, hash) {

    constructor(transaction: VoteTransaction) : this(
        transaction.timestamp,
        transaction.amount,
        transaction.fee,
        transaction.recipientAddress,
        transaction.senderKey,
        transaction.senderAddress,
        transaction.senderSignature,
        transaction.hash,
        transaction.getVoteType(),
        DelegateInfo(transaction.delegateHost, transaction.delegatePort)
    )

}