package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.URewardTransaction

class RewardTransactionDto(
    data: RewardTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<RewardTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: URewardTransaction) : this(
        RewardTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

}
