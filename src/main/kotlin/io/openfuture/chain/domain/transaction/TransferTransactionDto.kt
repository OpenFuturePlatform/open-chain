package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction

class TransferTransactionDto(
    data: TransferTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<TransferTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: TransferTransaction) : this(
        TransferTransactionData(),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

}
