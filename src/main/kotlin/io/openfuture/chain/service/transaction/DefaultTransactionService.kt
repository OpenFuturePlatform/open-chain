package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.TransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultTransactionService<Entity : Transaction, Data : BaseTransactionData>(
    protected val repository: TransactionRepository<Entity>,
    protected val walletService: WalletService,
    private val nodeClock: NodeClock,
    private val entityConverter: TransactionEntityConverter<Entity, Data>
) : TransactionService<Entity, Data> {

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

    private fun saveAndBroadcast(tx: Entity): Entity {
        return repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

}