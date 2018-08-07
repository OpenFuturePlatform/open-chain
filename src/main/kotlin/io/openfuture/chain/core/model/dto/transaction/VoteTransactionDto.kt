package io.openfuture.chain.core.model.dto.transaction

import io.openfuture.chain.core.model.dto.transaction.data.VoteTransactionData
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.core.annotation.NoArgConstructor

@NoArgConstructor
class VoteTransactionDto(
    data: VoteTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<VoteTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: VoteTransaction) : this(
        VoteTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress, tx.getVoteType().getId(), tx.delegateKey),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

    constructor(tx: UVoteTransaction) : this(
        VoteTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress, tx.getVoteType().getId(), tx.delegateKey),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

    fun toEntity(): VoteTransaction = VoteTransaction(
        timestamp,
        data.amount,
        data.fee,
        data.recipientAddress,
        data.senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        data.voteTypeId,
        data.delegateKey
    )

    fun toUEntity(): UVoteTransaction = UVoteTransaction(
        timestamp,
        data.amount,
        data.fee,
        data.recipientAddress,
        data.senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        data.voteTypeId,
        data.delegateKey
    )

    override fun getDataInstance(): VoteTransactionData = VoteTransactionData::class.java.newInstance()

}