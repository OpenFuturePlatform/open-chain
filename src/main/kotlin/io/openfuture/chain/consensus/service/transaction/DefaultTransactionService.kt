package io.openfuture.chain.consensus.service.transaction

import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.service.TransactionService
import io.openfuture.chain.consensus.service.WalletService
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.model.entity.transaction.UTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.CommonTransactionService
import org.springframework.beans.factory.annotation.Autowired

abstract class DefaultTransactionService<Entity : Transaction, UEntity : UTransaction>(
    private val repository: TransactionRepository<Entity>,
    private val uRepository: UTransactionRepository<UEntity>
) : TransactionService<Entity, UEntity> {

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var commonService: CommonTransactionService

    protected fun getUnconfirmed(hash: String): UEntity = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not found")

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