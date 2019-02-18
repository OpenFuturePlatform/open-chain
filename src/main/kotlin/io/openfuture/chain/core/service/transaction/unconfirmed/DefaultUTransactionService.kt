package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.UTransactionService
import io.openfuture.chain.core.service.transaction.validation.DelegateTransactionValidator
import io.openfuture.chain.core.service.transaction.validation.TransferTransactionValidator
import io.openfuture.chain.core.service.transaction.validation.VoteTransactionValidator
import io.openfuture.chain.core.service.transaction.validation.pipeline.TransactionValidationPipeline
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class DefaultUTransactionService<uT : UnconfirmedTransaction>(
    private val uRepository: UTransactionRepository<uT>
) : UTransactionService<uT> {

    @Autowired private lateinit var networkService: NetworkApiService
    @Autowired private lateinit var repository: TransactionRepository<Transaction>
    @Autowired private lateinit var delegateTransactionValidator: DelegateTransactionValidator
    @Autowired private lateinit var transferTransactionValidator: TransferTransactionValidator
    @Autowired private lateinit var voteTransactionValidator: VoteTransactionValidator


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
    override fun add(uTx: uT, unconfirmedBalance: Long): uT {
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

            when (uTx) {
                is UnconfirmedDelegateTransaction -> {
                    val pipeline = TransactionValidationPipeline(delegateTransactionValidator.checkNew(unconfirmedBalance))
                    delegateTransactionValidator.validate(DelegateTransaction.of(uTx), pipeline)
                }
                is UnconfirmedTransferTransaction -> {
                    val pipeline = TransactionValidationPipeline(transferTransactionValidator.checkNew(unconfirmedBalance))
                    transferTransactionValidator.validate(TransferTransaction.of(uTx), pipeline)
                }
                is UnconfirmedVoteTransaction -> {
                    val pipeline = TransactionValidationPipeline(voteTransactionValidator.checkNew(unconfirmedBalance))
                    voteTransactionValidator.validate(VoteTransaction.of(uTx), pipeline)
                }
                else -> throw IllegalStateException("Wrong type")
            }

            val savedUtx = uRepository.saveAndFlush(uTx)
            networkService.broadcast(savedUtx.toMessage())
            return savedUtx
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}