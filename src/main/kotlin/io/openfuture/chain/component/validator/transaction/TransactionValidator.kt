package io.openfuture.chain.component.validator.transaction

import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction

interface TransactionValidator<Entity : BaseTransaction, Data : BaseTransactionData> {

    fun validate(dto: BaseTransactionDto<Data>)

    fun validate(request: BaseTransactionRequest<Data>)

}