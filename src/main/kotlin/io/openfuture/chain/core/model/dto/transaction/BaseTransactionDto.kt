package io.openfuture.chain.core.model.dto.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction

abstract class BaseTransactionDto (
    var timestamp: Long,
    var fee: Long,
    var senderAddress: String,
    var senderPublicKey: String,
    var senderSignature: String,
    var hash: String
)
