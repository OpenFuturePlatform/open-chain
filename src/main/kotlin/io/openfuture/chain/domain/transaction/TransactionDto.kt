package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.entity.dictionary.TransactionType

open class TransactionDto(
        var timestamp: Long,
        var hash: String,
        var type: TransactionType
) {

    constructor(transaction: Transaction) : this(
            transaction.timestamp,
            transaction.hash,
            transaction.getType()
    )

}
