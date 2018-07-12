package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.util.DictionaryUtils

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
    val delegateKey: String,
    val weight: Int
) : BaseTransactionDto(timestamp, amount, recipientKey, recipientAddress, senderKey, senderAddress,
    senderSignature, hash) {

    constructor(transaction: VoteTransaction) : this(
        transaction.timestamp,
        transaction.amount,
        transaction.recipientKey,
        transaction.recipientAddress,
        transaction.senderKey,
        transaction.senderAddress,
        transaction.senderSignature,
        transaction.hash,
        DictionaryUtils.valueOf(VoteType::class.java, transaction.voteTypeId),
        transaction.delegateKey,
        transaction.weight
    )

}