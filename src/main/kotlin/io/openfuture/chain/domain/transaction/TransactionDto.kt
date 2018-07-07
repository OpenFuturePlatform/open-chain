package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.Transaction

abstract class TransactionDto(
        var timestamp: Long,
        var amount: Long,
        var recipientKey: String,
        var senderKey: String,
        var senderSignature: String,
        var hash: String
) {

    abstract fun toEntity(): Transaction

}
