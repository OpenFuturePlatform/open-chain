package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.base.BaseTransaction

abstract class EmbeddedTransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData>
    : BaseTransactionEntityConverter<Entity, Data>() {

    abstract fun toEntity(timestamp: Long, data: Data): Entity

}