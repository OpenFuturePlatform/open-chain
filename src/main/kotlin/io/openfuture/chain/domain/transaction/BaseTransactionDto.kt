package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.BaseTransactionData

abstract class BaseTransactionDto<Data: BaseTransactionData>(
    val data: Data,
    val timestamp: Long,
    val senderPublicKey: String,
    val senderSignature: String,
    val hash: String
)
