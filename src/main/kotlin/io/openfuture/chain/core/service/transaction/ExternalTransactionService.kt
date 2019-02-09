package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.TransactionValidatorManager
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.beans.factory.annotation.Autowired

abstract class ExternalTransactionService<T : Transaction, U : UnconfirmedTransaction>(
    protected val repository: TransactionRepository<T>,
    protected val unconfirmedRepository: UTransactionRepository<U>
) {

    @Autowired protected lateinit var stateManager: StateManager
    @Autowired protected lateinit var baseService: TransactionService
    @Autowired private lateinit var cryptoService: CryptoService
    @Autowired private lateinit var networkService: NetworkApiService
    @Autowired private lateinit var transactionValidatorManager: TransactionValidatorManager


    protected fun add(utx: U): U {
        val persistTx = repository.findOneByHash(utx.hash)
        if (null != persistTx) {
            throw CoreException("Transaction already handled")
        }

        val persistUtx = unconfirmedRepository.findOneByHash(utx.hash)
        if (null != persistUtx) {
            return persistUtx
        }

        transactionValidatorManager.validateNew(utx)

        val savedUtx = unconfirmedRepository.save(utx)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    protected fun confirm(utx: U, tx: T): T {
        unconfirmedRepository.delete(utx)
        return repository.save(tx)
    }

    protected fun getReceipt(hash: String, results: List<ReceiptResult>): Receipt {
        val receipt = Receipt(hash)
        receipt.setResults(results)
        return receipt
    }

}
