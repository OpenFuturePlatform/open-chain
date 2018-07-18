package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultBaseTransactionService<Entity : BaseTransaction>(
    protected val repository: BaseTransactionRepository<Entity>,
    protected val walletService: WalletService
) : BaseTransactionService<Entity> {

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<Entity> {
        return repository.findAllByBlockIsNull()
    }

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

        beforeAddToBlock(persistTransaction)
        return repository.save(persistTransaction)
    }

    protected abstract fun beforeAddToBlock(entity: Entity)

    protected fun updateWalletBalance(senderAddress: String, recipientAddress: String, amount: Double) {
        walletService.updateBalance(senderAddress, recipientAddress, amount)
    }

}