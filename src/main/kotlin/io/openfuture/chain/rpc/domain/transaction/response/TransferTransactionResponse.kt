package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction

class TransferTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    hash: String,
    val amount: Long,
    val recipientAddress: String,
    blockHash: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey, hash, blockHash) {

    constructor(tx: UnconfirmedTransferTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.hash,
        tx.payload.amount,
        tx.payload.recipientAddress
    )

    constructor(tx: TransferTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.hash,
        tx.payload.amount,
        tx.payload.recipientAddress,
        tx.block.hash
    )

}