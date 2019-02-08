package io.openfuture.chain.core.model.entity.dictionary

enum class TransferTransactionType {
    FUND,
    DEPLOY,
    EXECUTE;

    companion object {
        fun getType(recipientAddress: String?, data: String?): TransferTransactionType = when {
            data == null && recipientAddress != null -> FUND
            data != null && recipientAddress == null -> DEPLOY
            data != null && recipientAddress != null -> EXECUTE
            else -> throw IllegalStateException("Invalid transaction type")
        }
    }

}