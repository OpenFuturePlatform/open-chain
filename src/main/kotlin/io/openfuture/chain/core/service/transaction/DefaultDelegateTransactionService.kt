package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    uRepository: UDelegateTransactionRepository,
    private val consensusProperties: ConsensusProperties
) : ExternalTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction>(repository, uRepository), DelegateTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultDelegateTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getUnconfirmedCount(): Long = unconfirmedRepository.count()

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): DelegateTransaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedDelegateTransaction> =
        unconfirmedRepository.findAllByOrderByFeeDesc(request)

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction =
        unconfirmedRepository.findOneByHash(hash) ?: throw NotFoundException("Transaction with hash $hash not found")

    @BlockchainSynchronized
    @Transactional
    override fun add(message: DelegateTransactionMessage) {
        BlockchainLock.writeLock.lock()
        try {
            super.add(UnconfirmedDelegateTransaction.of(message))
        } catch (ex: CoreException) {
            log.debug(ex.message)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction {
        BlockchainLock.writeLock.lock()
        try {
            return super.add(UnconfirmedDelegateTransaction.of(request))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @Transactional
    override fun commit(transaction: DelegateTransaction): DelegateTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val tx = repository.findOneByHash(transaction.hash)
            if (null != tx) {
                return tx
            }

            val utx = unconfirmedRepository.findOneByHash(transaction.hash)
            if (null != utx) {
                return confirm(utx, transaction)
            }

            return repository.save(transaction)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(message: DelegateTransactionMessage, delegateWallet: String): Receipt {
        stateManager.updateWalletBalanceByAddress(message.senderAddress, -(message.amount + message.fee))
        stateManager.updateWalletBalanceByAddress(consensusProperties.genesisAddress!!, message.amount)
        stateManager.addDelegate(message.delegateKey, message.senderAddress, message.timestamp)

        return generateReceipt(message, delegateWallet)
    }

    private fun generateReceipt(message: DelegateTransactionMessage, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(message.senderAddress, consensusProperties.genesisAddress!!, message.amount, message.delegateKey),
            ReceiptResult(message.senderAddress, delegateWallet, message.fee)
        )
        return getReceipt(message.hash, results)
    }

}