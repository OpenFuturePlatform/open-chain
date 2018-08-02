package io.openfuture.chain.network.message.application.transaction

import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.network.message.application.transaction.data.TransferTransactionData

@NoArgConstructor
class TransferTransactionMessage(
    data: TransferTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionMessage<TransferTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: TransferTransaction) : this(
        TransferTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

    constructor(tx: UTransferTransaction) : this(
        TransferTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

    fun toEntity(): TransferTransaction = TransferTransaction(
        timestamp,
        data.amount,
        data.fee,
        data.recipientAddress,
        data.senderAddress,
        senderPublicKey,
        senderSignature,
        hash
    )

    fun toUEntity(): UTransferTransaction = UTransferTransaction(
        timestamp,
        data.amount,
        data.fee,
        data.recipientAddress,
        data.senderAddress,
        senderPublicKey,
        senderSignature,
        hash
    )

    override fun getDataInstance(): TransferTransactionData = TransferTransactionData::class.java.newInstance()

}
