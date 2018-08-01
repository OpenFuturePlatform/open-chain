package io.openfuture.chain.rpc.domain.transaction

import io.openfuture.chain.core.model.dto.transaction.data.TransferTransactionData
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.util.TransactionUtils

class TransferTransactionRequest(
    data: TransferTransactionData
) : BaseTransactionRequest<UTransferTransaction, TransferTransactionData>(data) {

    fun toUEntity(timestamp: Long): UTransferTransaction = UTransferTransaction(
        timestamp,
        data!!.amount,
        data!!.fee,
        data!!.recipientAddress,
        data!!.senderAddress,
        senderPublicKey!!,
        senderSignature!!,
        TransactionUtils.createHash(data!!, senderPublicKey!!, senderSignature!!)
    )

}