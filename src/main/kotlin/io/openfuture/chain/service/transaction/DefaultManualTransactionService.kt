package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.ManualTransactionService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultManualTransactionService<Entity : BaseTransaction, Data : BaseTransactionData>(
    repository: BaseTransactionRepository<Entity>,
    override val entityConverter: ManualTransactionEntityConverter<Entity, Data>
) : DefaultCommonTransactionService<Entity, Data>(repository, entityConverter),
    ManualTransactionService<Entity, Data> {

    @Transactional
    override fun add(request: BaseTransactionRequest<Data>): Entity {
        validate(request)
        return saveAndBroadcast(entityConverter.toEntity(nodeClock.networkTime(), request))
    }

    open fun validate(request: BaseTransactionRequest<Data>) {
        commonValidate(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}