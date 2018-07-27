package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction

class DelegateTransactionDto(
    data: DelegateTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<UDelegateTransaction, DelegateTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: UDelegateTransaction) : this(
        DelegateTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress, tx.delegateKey),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

    override fun toEntity(): UDelegateTransaction = UDelegateTransaction(
        timestamp,
        data.amount,
        data.fee,
        data.recipientAddress,
        data.senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        data.delegateKey
    )

    override fun getDataInstance(): DelegateTransactionData = DelegateTransactionData::class.java.newInstance()

}