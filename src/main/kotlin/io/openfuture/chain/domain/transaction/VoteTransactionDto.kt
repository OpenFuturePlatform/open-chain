package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.VoteTransaction

class VoteTransactionDto(
    data: VoteTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<VoteTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: VoteTransaction) : this(
        VoteTransactionData(tx.amount, tx.recipientAddress, tx.senderAddress, tx.getVoteType(), tx.delegateKey),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

}