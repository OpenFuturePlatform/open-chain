package io.openfuture.chain.rpc.domain.transaction

import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.consensus.util.TransactionUtils
import io.openfuture.chain.consensus.model.dto.transaction.data.TransferTransactionData

class TransferTransactionRequest(
    data: TransferTransactionData
) : BaseTransactionRequest<UTransferTransaction, TransferTransactionData>(data) {

    override fun toEntity(timestamp: Long): UTransferTransaction = UTransferTransaction(
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