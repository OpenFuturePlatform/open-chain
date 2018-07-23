package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.entity.transaction.BaseTransaction

interface TransactionEntityConverter<Entity : BaseTransaction, Data : BaseTransactionData> {

    fun toEntity(dto: BaseTransactionDto<Data>): Entity

    fun toEntity(timestamp: Long, request: BaseTransactionRequest<Data>): Entity

}