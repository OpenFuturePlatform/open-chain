package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBaseTransactionService(
    private val repository: BaseTransactionRepository<BaseTransaction>
) : BaseTransactionService {

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<BaseTransaction> {
        return repository.findAllByBlockIsNull()
    }

    @Transactional
    override fun save(transaction: Entity): Entity = repository.save(transaction)

    @Transactional
    override fun save(transactions: List<Entity>): List<Entity> = repository.saveAll(transactions)

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.existsByHash(hash)

}