package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.base.BaseTransaction

abstract class BaseTransactionEntityConverter<Entity : BaseTransaction, Data: BaseTransactionData> {

    abstract fun toEntity(dto: BaseTransactionDto<Data>): Entity

}