package io.openfuture.chain.component.converter.transaction

import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.entity.transaction.BaseTransaction

interface TransactionEntityConverter<Entity : BaseTransaction, Dto : BaseTransactionDto, Req : BaseTransactionRequest> {

    fun toEntity(dto: Dto): Entity

    fun toEntity(timestamp: Long, request: Req): Entity

}