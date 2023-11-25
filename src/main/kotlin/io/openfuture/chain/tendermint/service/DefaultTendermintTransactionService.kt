package io.openfuture.chain.tendermint.service

import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransaction
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransferTransaction
import io.openfuture.chain.core.repository.TendermintTransactionRepository
import io.openfuture.chain.core.service.TendermintTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.tendermint.handler.TendermintTransactionValidationPipeline
import io.openfuture.chain.tendermint.handler.TendermintTransferTransactionValidator
import io.openfuture.chain.tendermint.repository.TendermintTransactionsJdbcRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class DefaultTendermintTransactionService<uT : TendermintTransferTransaction>(
    private val uRepository: TendermintTransactionRepository<uT>,
    private val jdbcRepository: TendermintTransactionsJdbcRepository
) : TendermintTransactionService<uT> {

    @Autowired
    private lateinit var repository: TendermintTransactionRepository<TendermintTransaction>
    @Autowired private lateinit var transferTransactionValidator: TendermintTransferTransactionValidator
    override fun getAll(): List<uT> {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findAll()
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

    override fun check(uTx: uT, unconfirmedBalance: Long): Boolean {
        println("check transaction")
        return try {
            val pipeline = TendermintTransactionValidationPipeline(transferTransactionValidator.checkNew(unconfirmedBalance))
            transferTransactionValidator.validate(uTx, pipeline)
            true
        } catch (e: RuntimeException){
            println("Error : ${e.message}")
            false
        }
    }

    override fun add(uTx: uT, unconfirmedBalance: Long): Boolean {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(uTx.hash)
            if (null != persistTx) {
                //throw CoreException("Transaction already handled")
                return false
            }

            val savedUtx = jdbcRepository.save(uTx)
            uTx.id = savedUtx.id
            return true

        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}