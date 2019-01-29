package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INSUFFICIENT_ACTUAL_BALANCE
import io.openfuture.chain.core.model.entity.transaction.confirmed.DeployTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDeployTransaction
import io.openfuture.chain.core.repository.DeployTransactionRepository
import io.openfuture.chain.core.repository.UDeployTransactionRepository
import io.openfuture.chain.core.service.SmartContractTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.DeployTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.DeployTransactionRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultSmartContractTransactionService(
    repository: DeployTransactionRepository,
    uRepository: UDeployTransactionRepository
) : ExternalTransactionService<DeployTransaction, UnconfirmedDeployTransaction>(repository, uRepository), SmartContractTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultSmartContractTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getUnconfirmedCount(): Long = unconfirmedRepository.count()

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): DeployTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedDeployTransaction> =
        unconfirmedRepository.findAllByOrderByHeaderFeeDesc(request)

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedDeployTransaction =
        unconfirmedRepository.findOneByFooterHash(hash)
            ?: throw NotFoundException("Transaction with hash $hash not found")

    @BlockchainSynchronized
    @Transactional
    override fun add(message: DeployTransactionMessage) {
        BlockchainLock.writeLock.lock()
        try {
            super.add(UnconfirmedDeployTransaction.of(message))
        } catch (ex: CoreException) {
            log.debug(ex.message)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: DeployTransactionRequest): UnconfirmedDeployTransaction {
        BlockchainLock.writeLock.lock()
        try {
            return super.add(UnconfirmedDeployTransaction.of(request))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @Transactional
    override fun commit(transaction: DeployTransaction): DeployTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val tx = repository.findOneByFooterHash(transaction.footer.hash)
            if (null != tx) {
                return tx
            }

            val utx = unconfirmedRepository.findOneByFooterHash(transaction.footer.hash)
            if (null != utx) {
                return confirm(utx, transaction)
            }

            return save(transaction)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun verify(message: DeployTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedDeployTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    override fun updateState(message: DeployTransactionMessage) {
        walletStateService.updateBalanceByAddress(message.senderAddress, -message.fee)
    }

    @Transactional
    override fun save(tx: DeployTransaction): DeployTransaction = super.save(tx)

    override fun validate(utx: UnconfirmedDeployTransaction) {
        super.validate(utx)

        if (utx.header.fee < 0) {
            throw ValidationException("Fee should not be less than 0")
        }

    }

    @Transactional(readOnly = true)
    override fun validateNew(utx: UnconfirmedDeployTransaction) {
        if (!isValidActualBalance(utx.header.senderAddress, utx.header.fee)) {
            throw ValidationException("Insufficient actual balance", INSUFFICIENT_ACTUAL_BALANCE)
        }
    }

}