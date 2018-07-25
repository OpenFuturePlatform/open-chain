package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction

class VoteTransactionDto(
    data: VoteTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<VoteTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: UVoteTransaction) : this(
        VoteTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress, tx.getVoteType(), tx.delegateKey),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

}