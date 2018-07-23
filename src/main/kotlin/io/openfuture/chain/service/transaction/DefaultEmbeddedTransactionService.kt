package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.EmbeddedTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultEmbeddedTransactionService<Entity : BaseTransaction, Data : BaseTransactionData>(
    repository: BaseTransactionRepository<Entity>,
    walletService: WalletService,
    entityConverter: TransactionEntityConverter<Entity, Data>
) : DefaultBaseTransactionService<Entity, Data>(repository, walletService, entityConverter),
    EmbeddedTransactionService<Entity, Data> {

    @Transactional
    override fun add(dto: BaseTransactionDto<Data>): Entity = repository.save(entityConverter.toEntity(dto))

}