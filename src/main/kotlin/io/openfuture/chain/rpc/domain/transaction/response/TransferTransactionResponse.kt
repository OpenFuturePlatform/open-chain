package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.rpc.domain.ReceiptResultResponse

class TransferTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    hash: String,
    val status: Boolean,
    val amount: Long,
    val recipientAddress: String? = null,
    val data: String? = null,
    val results: List<ReceiptResultResponse>? = null,
    blockHash: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey, hash, blockHash) {

    constructor(tx: UnconfirmedTransferTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.signature,
        tx.publicKey,
        tx.hash,
        true,
        tx.getPayload().amount,
        tx.getPayload().recipientAddress,
        tx.getPayload().data
    )

    constructor(tx: TransferTransaction, receipt: Receipt) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.signature,
        tx.publicKey,
        tx.hash,
        receipt.isSuccessful(),
        tx.getPayload().amount,
        tx.getPayload().recipientAddress,
        tx.getPayload().data,
        receipt.getResults().map { ReceiptResultResponse(it) },
        tx.block?.hash
    )

}