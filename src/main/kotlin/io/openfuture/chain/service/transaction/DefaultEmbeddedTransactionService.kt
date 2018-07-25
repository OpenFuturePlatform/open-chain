package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.EmbeddedTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.EmbeddedTransactionService

abstract class DefaultEmbeddedTransactionService<Entity : Transaction, Data : BaseTransactionData>(
    repository: TransactionRepository<Entity>,
    entityConverter: EmbeddedTransactionEntityConverter<Entity, Data>
) : DefaultCommonTransactionService<Entity, Data, EmbeddedTransactionEntityConverter<Entity, Data>>(repository, entityConverter),
    EmbeddedTransactionService<Entity, Data>