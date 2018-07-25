package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.EmbeddedTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.EmbeddedUTransactionService

abstract class DefaultEmbeddedUTransactionService<Entity : UTransaction, Data : BaseTransactionData>(
    repository: UTransactionRepository<Entity>,
    entityConverter: EmbeddedTransactionEntityConverter<Entity, Data>
) : DefaultCommonUTransactionService<Entity, Data, EmbeddedTransactionEntityConverter<Entity, Data>>(repository, entityConverter),
    EmbeddedUTransactionService<Entity, Data>