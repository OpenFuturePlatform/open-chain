package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionValidatorManager
import io.openfuture.chain.core.service.UTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class DefaultUTransactionService<uT : UnconfirmedTransaction, uR : UTransactionRepository<uT>>(
    private val uRepository: uR
) : UTransactionService<uT> {

    @Autowired private lateinit var transactionValidatorManager: TransactionValidatorManager
    @Autowired private lateinit var networkService: NetworkApiService
    @Autowired private lateinit var repository: TransactionRepository<Transaction>


    override fun getAll(): List<uT> {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findAll()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getAll(request: PageRequest): List<uT> {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findAllByOrderByFeeDesc(request)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getAllBySenderAddress(address: String): List<uT> {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findAllBySenderAddress(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(uTx: uT): uT {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(uTx.hash)
            if (null != persistTx) {
                throw CoreException("Transaction already handled")
            }

            val persistUtx = uRepository.findOneByHash(uTx.hash)
            if (null != persistUtx) {
                return persistUtx
            }

            transactionValidatorManager.validateNew(uTx)

            val savedUtx = uRepository.saveAndFlush(uTx)
            networkService.broadcast(savedUtx.toMessage())
            return savedUtx
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}