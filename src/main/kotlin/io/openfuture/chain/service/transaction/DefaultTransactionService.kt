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


    override fun save(tx: Entity): Entity = repository.save(tx)

    @Suppress("UNCHECKED_CAST")
    @Transactional
    override fun toBlock(tx: UEntity, block: MainBlock): Entity {
        val uTx = getUnconfirmedTransaction(tx.hash)
        val persistTx = uTx.toConfirmed() as Entity
        persistTx.block = block
        return save(persistTx)
    }

    private fun getUnconfirmedTransaction(hash: String): UEntity = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

}