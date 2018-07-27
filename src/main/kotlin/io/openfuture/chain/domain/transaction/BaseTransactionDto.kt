package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction

abstract class BaseTransactionDto<Entity: UTransaction, Data: BaseTransactionData>(
    val data: Data,
    val timestamp: Long,
    val senderPublicKey: String,
    val senderSignature: String,
    val hash: String
) {

    abstract fun toEntity(): Entity

}
