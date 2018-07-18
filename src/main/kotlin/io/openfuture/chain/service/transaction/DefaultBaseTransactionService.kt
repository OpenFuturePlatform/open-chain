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

    protected fun commonAddToBlock(tx: Entity, block: MainBlock): Entity {
        val persistTx = this.get(tx.hash)

        if (null != persistTx.block) {
            throw LogicException("Transaction with hash: ${tx.hash} already belong to block!")
        }
        persistTx.block = block
        walletService.updateBalance(persistTx.senderAddress, persistTx.recipientAddress, persistTx.amount)
        return repository.save(persistTx)
    }

}