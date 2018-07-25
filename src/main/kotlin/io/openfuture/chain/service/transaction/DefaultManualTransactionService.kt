package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.ManualTransactionService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultManualTransactionService<Entity : Transaction, Data : BaseTransactionData>(
    repository: TransactionRepository<Entity>,
    entityConverter: ManualTransactionEntityConverter<Entity, Data>
) : DefaultCommonTransactionService<Entity, Data, ManualTransactionEntityConverter<Entity, Data>>(repository, entityConverter),
    ManualTransactionService<Entity, Data> {

    @Transactional
    override fun add(request: BaseTransactionRequest<Data>): Entity {
        validate(request)
        return saveAndBroadcast(entityConverter.toEntity(nodeClock.networkTime(), request))
    }

    protected abstract fun validate(request: BaseTransactionRequest<Data>)

    protected fun baseValidate(request: BaseTransactionRequest<Data>) {
        commonValidate(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}