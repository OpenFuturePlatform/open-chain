package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.ManualUTransactionService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultManualUTransactionService<Entity : UTransaction, Data : BaseTransactionData>(
    repository: UTransactionRepository<Entity>,
    entityConverter: ManualTransactionEntityConverter<Entity, Data>
) : DefaultUTransactionService<Entity, Data, ManualTransactionEntityConverter<Entity, Data>>(repository, entityConverter),
    ManualUTransactionService<Entity, Data> {

    @Transactional
    override fun add(request: BaseTransactionRequest<Data>): Entity {
        validate(request)
        return saveAndBroadcast(entityConverter.toEntity(nodeClock.networkTime(), request))
    }

    open fun validate(request: BaseTransactionRequest<Data>) {
        commonValidate(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}