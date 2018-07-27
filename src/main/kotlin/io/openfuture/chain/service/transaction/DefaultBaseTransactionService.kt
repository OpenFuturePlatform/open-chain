package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultBaseTransactionService<Entity : Transaction>(
    protected val repository: TransactionRepository<Entity>
) : BaseTransactionService<Entity> {

    @Transactional(readOnly = true)
    override fun get(hash: String): Entity = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional
    override fun toBlock(tx: Entity, block: MainBlock): Entity {
        tx.block = block
        return repository.save(tx)
    }
}