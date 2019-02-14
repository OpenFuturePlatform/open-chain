package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.ExternalTransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class DefaultExternalTransactionService<T : Transaction>(
    private val repository: TransactionRepository<T>
) : DefaultTransactionService<T>(repository), ExternalTransactionService<T> {

    @Autowired protected lateinit var uRepository: UTransactionRepository<UnconfirmedTransaction>


    override fun getAllByBlock(block: Block): List<T> = repository.findAllByBlock(block)

    protected fun confirm(uTx: UnconfirmedTransaction, tx: T): T {
        uRepository.delete(uTx)
        return repository.save(tx)
    }

}