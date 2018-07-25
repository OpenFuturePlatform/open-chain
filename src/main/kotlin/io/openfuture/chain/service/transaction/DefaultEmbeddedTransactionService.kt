package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.EmbeddedTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.EmbeddedTransactionService

abstract class DefaultEmbeddedTransactionService<Entity : BaseTransaction, Data : BaseTransactionData>(
    repository: BaseTransactionRepository<Entity>,
    entityConverter: EmbeddedTransactionEntityConverter<Entity, Data>
) : DefaultCommonTransactionService<Entity, Data, EmbeddedTransactionEntityConverter<Entity, Data>>(repository, entityConverter),
    EmbeddedTransactionService<Entity, Data>