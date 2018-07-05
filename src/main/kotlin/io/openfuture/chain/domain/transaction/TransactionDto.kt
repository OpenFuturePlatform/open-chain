package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.Transaction

data class TransactionDto(
        val data: TransactionData,
        var hash: String
) {

    constructor(transaction: Transaction) : this(
            TransactionData(transaction.amount, transaction.timestamp, transaction.recipientkey,
                    transaction.senderKey, transaction.signature),
            transaction.hash
    )

}
