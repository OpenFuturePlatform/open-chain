package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_DELEGATE
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateStateService
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
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val delegateStateService: DelegateStateService
) : ExternalTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction>(repository, uRepository), DelegateTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultDelegateTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getUnconfirmedCount(): Long = unconfirmedRepository.count()

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): DelegateTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedDelegateTransaction> =
        unconfirmedRepository.findAllByOrderByHeaderFeeDesc(request)

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction = unconfirmedRepository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

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
    override fun toBlock(transaction: DelegateTransaction, block: MainBlock): DelegateTransaction {
        return toBlock(transaction.toMessage(), block)
    }

    @Transactional
    override fun toBlock(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val tx = repository.findOneByFooterHash(message.hash)
            if (null != tx) {
                return tx
            }

            val utx = unconfirmedRepository.findOneByFooterHash(message.hash)
            if (null != utx) {
                return confirm(utx, DelegateTransaction.of(utx, block))
            }

            return this.save(DelegateTransaction.of(message, block))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun updateState(message: DelegateTransactionMessage) {
        walletStateService.updateBalanceByAddress(message.senderAddress, -(message.amount + message.fee))
        walletStateService.updateBalanceByAddress(consensusProperties.genesisAddress!!, message.amount)
        delegateStateService.addDelegate(message.senderPublicKey)
    }

    override fun verify(message: DelegateTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedDelegateTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: DelegateTransaction): DelegateTransaction {
        delegateService.save(Delegate(tx.payload.delegateKey, tx.header.senderAddress, tx.header.timestamp))
        return super.save(tx)
    }

    override fun validate(utx: UnconfirmedDelegateTransaction) {
        super.validate(utx)

        if (utx.header.fee != consensusProperties.feeDelegateTx!!) {
            throw ValidationException("Fee should be ${consensusProperties.feeDelegateTx!!}")
        }

        if (utx.payload.amount != consensusProperties.amountDelegateTx!!) {
            throw ValidationException("Amount should be ${consensusProperties.amountDelegateTx!!}")
        }
    }

    @Transactional(readOnly = true)
    override fun validateNew(utx: UnconfirmedDelegateTransaction) {
        if (!isValidActualBalance(utx.header.senderAddress, utx.payload.amount + utx.header.fee)) {
            throw ValidationException("Insufficient actual balance", ExceptionType.INSUFFICIENT_ACTUAL_BALANCE)
        }

        if (isAlreadyDelegate(utx.payload.delegateKey)) {
            throw ValidationException("Node ${utx.payload.delegateKey} already registered as delegate", ALREADY_DELEGATE)
        }

        if (isAlreadySendRequest(utx.payload.delegateKey)) {
            throw ValidationException("Node ${utx.payload.delegateKey} already send request to become delegate", ALREADY_DELEGATE)
        }
    }

    private fun isAlreadyDelegate(delegateKey: String): Boolean = delegateService.isExistsByPublicKey(delegateKey)

    private fun isAlreadySendRequest(delegateKey: String): Boolean =
        unconfirmedRepository.findAll().any { it.payload.delegateKey == delegateKey }

}