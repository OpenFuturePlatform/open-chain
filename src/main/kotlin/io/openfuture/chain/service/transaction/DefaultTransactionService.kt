package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.CommonTransactionService
import io.openfuture.chain.service.TransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class DefaultTransactionService<Entity : Transaction, UEntity : UTransaction>(
    private val repository: TransactionRepository<Entity>,
    private val uRepository: UTransactionRepository<UEntity>
) : TransactionService<Entity, UEntity> {

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var commonService: CommonTransactionService

    @Transactional(readOnly = true)
    override fun get(hash: String): UEntity = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

    protected fun processAndSave(tx: Entity, block: MainBlock) {
        if (commonService.isExists(tx.hash)) {
            return
        }

        tx.block = block
        updateBalance(tx)
        repository.save(tx)
    }

    private fun updateBalance(tx: Entity) {
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
    }

}