package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import org.springframework.data.querydsl.QPageRequest
import org.springframework.transaction.annotation.Transactional

abstract class DefaultBaseTransactionService<Entity : BaseTransaction>(
    protected val repository: BaseTransactionRepository<Entity>
) : BaseTransactionService<Entity> {

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<Entity> = repository.findAllByBlockIsNull()

    @Transactional(readOnly = true)
    override fun getAllPending(limit: Int): MutableSet<Entity> {
        val pageable = QPageRequest(0, limit)
        return HashSet(repository.findAll(pageable).content)
    }

    override fun getPendingFirstWithLimit(limit: Int): MutableSet<Entity>
        = getAllPending(limit)

    @Transactional(readOnly = true)
    override fun get(hash: String): Entity = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional
    override fun addToBlock(hash: String, block: MainBlock): Entity {
        val persistTransaction = this.get(hash)
        if (null != persistTransaction.block) {
            throw LogicException("Transaction with hash: $hash already belong to block!")
        }

        persistTransaction.block = block
        return repository.save(persistTransaction)
    }

}