package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.util.TransactionUtils

class TransferTransactionDto(
    timestamp: Long,
    amount: Double,
    recipientKey: String,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String
): BaseTransactionDto(timestamp, amount, recipientKey, recipientAddress, senderKey, senderAddress, senderSignature, hash) {

    companion object {
        fun of(networkTime: Long, data: TransferTransactionData) = TransferTransactionDto(
            networkTime,
            data.amount!!,
            data.recipientKey!!,
            data.recipientAddress!!,
            data.senderKey!!,
            data.senderAddress!!,
            data.senderSignature!!,
            TransactionUtils.calculateHash(networkTime, data)
        )
    }

}
