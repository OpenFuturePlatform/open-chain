package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.service.ExternalTransactionService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionValidatorManager
import io.openfuture.chain.core.service.UTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class DefaultExternalTransactionService<T : Transaction, uT : UnconfirmedTransaction,
    R : TransactionRepository<T>, uS : UTransactionService<uT>>(
    private val repository: R,
    private val uTransactionService: uS
) : DefaultTransactionService<T, R>(repository), ExternalTransactionService<T, uT> {

    @Autowired private lateinit var transactionValidatorManager: TransactionValidatorManager
    @Autowired private lateinit var networkService: NetworkApiService
    @Autowired protected lateinit var stateManager: StateManager


    @BlockchainSynchronized
    @Transactional
    override fun add(uTx: uT): uT {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(uTx.hash)
            if (null != persistTx) {
                throw CoreException("Transaction already handled")
            }

            val persistUtx = uTransactionService.findByHash(uTx.hash)
            if (null != persistUtx) {
                return persistUtx
            }

            transactionValidatorManager.validateNew(uTx)

            val savedUtx = uTransactionService.save(uTx)
            networkService.broadcast(savedUtx.toMessage())
            return savedUtx
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    protected fun confirm(uTx: uT, tx: T): T {
        uTransactionService.remove(uTx)
        return repository.save(tx)
    }

    protected fun getReceipt(hash: String, results: List<ReceiptResult>): Receipt {
        val receipt = Receipt(hash)
        receipt.setResults(results)

        return receipt
    }

}