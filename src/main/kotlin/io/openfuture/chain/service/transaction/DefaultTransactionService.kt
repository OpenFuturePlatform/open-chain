package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.TransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class DefaultTransactionService<Entity : Transaction, UEntity : UTransaction>(
    protected val repository: TransactionRepository<Entity>,
    protected val uRepository: UTransactionRepository<UEntity>
) : TransactionService<Entity, UEntity> {

    @Autowired
    protected lateinit var walletService: WalletService

    @Transactional(readOnly = true)
    override fun get(hash: String): Entity = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional
    override fun deleteUnconfirmedAndSave(tx: Entity): Entity {
        val uTx = getUnconfirmedTransaction(tx.hash)
        updateBalance(uTx)
        uRepository.delete(uTx)
        return repository.save(tx)
    }

    @Transactional
    override fun toBlock(tx: Entity, block: MainBlock): Entity {
        tx.block = block
        return deleteUnconfirmedAndSave(tx)
    }

    private fun updateBalance(tx: UEntity) {
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
    }

    private fun getUnconfirmedTransaction(hash: String): UEntity = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

}