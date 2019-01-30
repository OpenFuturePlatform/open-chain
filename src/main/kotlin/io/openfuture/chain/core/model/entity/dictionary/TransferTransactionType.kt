package io.openfuture.chain.core.model.entity.dictionary

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.network.message.core.TransferTransactionMessage

enum class TransferTransactionType {
    FUND,
    DEPLOY,
    EXECUTE;

    companion object {
        fun getType(message: TransferTransactionMessage): TransferTransactionType = when {
            message.data == null && message.recipientAddress != null -> FUND
            message.data != null && message.recipientAddress == null -> DEPLOY
            message.data != null && message.recipientAddress != null -> EXECUTE
            else -> throw IllegalStateException("Invalid transaction type")
        }

        fun getType(payload: TransferTransactionPayload): TransferTransactionType = when {
            payload.data == null && payload.recipientAddress != null -> FUND
            payload.data != null && payload.recipientAddress == null -> DEPLOY
            payload.data != null && payload.recipientAddress != null -> EXECUTE
            else -> throw IllegalStateException("Invalid transaction type")
        }
    }

}