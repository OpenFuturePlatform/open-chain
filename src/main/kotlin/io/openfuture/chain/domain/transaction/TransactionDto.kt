package io.openfuture.chain.domain.transaction

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Transaction

data class TransactionDto(
        var data: TransactionData,
        var timestamp: Long,
        var hash: String
) {

    companion object {
        fun of(networkTime: Long, data: TransactionData): TransactionDto = TransactionDto(
                data,
                networkTime,
                HashUtils.generateHash(data.getByteData() + networkTime.toByte())
        )
    }

    constructor(transaction: Transaction) : this(
            TransactionData(transaction.amount, transaction.recipientKey, transaction.senderKey,
                    transaction.senderSignature, transaction.getPayload()),
            transaction.timestamp,
            transaction.hash
    )

}
