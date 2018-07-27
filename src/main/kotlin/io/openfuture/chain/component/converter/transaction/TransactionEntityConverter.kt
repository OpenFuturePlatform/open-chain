package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.Transaction

abstract class TransactionEntityConverter<Entity : Transaction, Data : BaseTransactionData>
    : BaseTransactionEntityConverter<Entity, Data>() {

    abstract fun toEntity(timestamp: Long, data: Data): Entity

}