package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class DefaultTransactionService<T : Transaction>(
    private val repository: TransactionRepository<T>
) : TransactionService<T> {

    @Autowired protected lateinit var stateManager: StateManager


    override fun getByHash(hash: String): T = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    override fun getAll(request: PageRequest): Page<T> = repository.findAll(request.toEntityRequest())

    protected fun getReceipt(hash: String, results: List<ReceiptResult>): Receipt =
        Receipt(hash, Receipt.generateResult(results))

}