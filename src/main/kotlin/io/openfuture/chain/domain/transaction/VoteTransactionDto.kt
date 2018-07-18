package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.domain.NetworkAddress

class VoteTransactionDto(timestamp: Long, amount: Double, recipientAddress: String, senderKey: String,
                         senderAddress: String, senderSignature: String, hash: String,
    val voteType: VoteType,
    val delegateDto: DelegateDto
) : BaseTransactionDto(timestamp, amount, recipientAddress, senderKey, senderAddress, senderSignature,
    hash) {

    constructor(tx: VoteTransaction) : this(
        tx.timestamp,
        tx.amount,
        tx.recipientAddress,
        tx.senderPublicKey,
        tx.senderAddress,
        tx.senderSignature,
        tx.hash,
        tx.getVoteType(),
        DelegateDto(
            tx.delegateKey,
            NetworkAddress(
                tx.delegateHost,
                tx.delegatePort
            )
        )
    )

}