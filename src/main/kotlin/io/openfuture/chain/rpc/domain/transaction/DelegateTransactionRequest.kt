package io.openfuture.chain.rpc.domain.transaction

import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.util.TransactionUtils

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