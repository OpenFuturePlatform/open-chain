package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction

abstract class UTransactionEntityConverter<Entity : UTransaction, Data : BaseTransactionData>
    : BaseTransactionEntityConverter<Entity, Data>() {

    abstract fun toEntity(timestamp: Long, request: BaseTransactionRequest<Data>): Entity

}