package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction

interface ManualTransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData>
    : TransactionEntityConverter<Entity, Data> {

    fun toEntity(timestamp: Long, request: BaseTransactionRequest<Data>): Entity

}