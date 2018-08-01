package io.openfuture.chain.rpc.domain.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.domain.application.transaction.data.DelegateTransactionData

class DelegateTransactionRequest(
    data: DelegateTransactionData
) : BaseTransactionRequest<UDelegateTransaction, DelegateTransactionData>(data) {

    override fun toEntity(timestamp: Long): UDelegateTransaction = UDelegateTransaction(
        timestamp,
        data!!.amount,
        data!!.fee,
        data!!.recipientAddress,
        data!!.senderAddress,
        senderPublicKey!!,
        senderSignature!!,
        TransactionUtils.createHash(data!!, senderPublicKey!!, senderSignature!!),
        data!!.delegateKey
    )

}