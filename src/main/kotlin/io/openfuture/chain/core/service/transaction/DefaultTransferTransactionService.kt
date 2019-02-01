package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INSUFFICIENT_ACTUAL_BALANCE
import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.*
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.Companion.getType
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransactionPageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository,
    private val contractService: ContractService
) : ExternalTransactionService<TransferTransaction, UnconfirmedTransferTransaction>(repository, uRepository), TransferTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultTransferTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getUnconfirmedCount(): Long = unconfirmedRepository.count()

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): TransferTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAll(request: TransactionPageRequest): Page<TransferTransaction> =
        repository.findAll(request.toEntityRequest())

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedTransferTransaction> =
        unconfirmedRepository.findAllByOrderByHeaderFeeDesc(request)

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction =
        unconfirmedRepository.findOneByFooterHash(hash)
            ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getByAddress(address: String, request: TransactionPageRequest): Page<TransferTransaction> =
        (repository as TransferTransactionRepository).findAllByHeaderSenderAddressOrPayloadRecipientAddress(address, address, request.toEntityRequest())

    @BlockchainSynchronized
    @Transactional
    override fun add(message: TransferTransactionMessage) {
        BlockchainLock.writeLock.lock()
        try {
            super.add(UnconfirmedTransferTransaction.of(message))
        } catch (ex: CoreException) {
            log.debug(ex.message)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction {
        BlockchainLock.writeLock.lock()
        try {
            return super.add(UnconfirmedTransferTransaction.of(request))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @Transactional
    override fun commit(transaction: TransferTransaction): TransferTransaction {
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

    override fun updateState(message: TransferTransactionMessage) {
        accountStateService.updateBalanceByAddress(message.senderAddress, -(message.amount + message.fee))

        val type = getType(message)
        if (FUND == type) {
            accountStateService.updateBalanceByAddress(message.recipientAddress!!, message.amount)
        }

        if (DEPLOY == type) {
            accountStateService.updateStorage(contractService.generateAddress(message.senderAddress), message.data!!)
        }
    }

    override fun generateReceipt(message: TransferTransactionMessage, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(
                message.senderAddress,
                message.recipientAddress ?: EMPTY,
                message.amount ,
                getType(message).toString()
            ), ReceiptResult(
                message.senderAddress,
                delegateWallet,
                message.fee
            )
        )

        return getReceipt(message.hash, results)
    }

    override fun verify(message: TransferTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedTransferTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: TransferTransaction): TransferTransaction {
        if (DEPLOY == getType(tx.payload)) {
            val address = contractService.generateAddress(tx.header.senderAddress)
            contractService.save(Contract(address, tx.header.senderAddress, tx.payload.data!!))
        }

        return super.save(tx)
    }

    override fun validate(utx: UnconfirmedTransferTransaction) {
        super.validate(utx)

        if (utx.header.fee < 0) {
            throw ValidationException("Fee should not be less than 0")
        }

        if (utx.payload.amount <= 0) {
            throw ValidationException("Amount should not be less than or equal to 0")
        }

        if (DEPLOY == getType(utx.payload)) {
            //todo validate bytecode
        }

        if (EXECUTE == getType(utx.payload)) {
            //todo validate method
        }

    }

    @Transactional(readOnly = true)
    override fun validateNew(utx: UnconfirmedTransferTransaction) {
        if (!isValidActualBalance(utx.header.senderAddress, utx.payload.amount + utx.header.fee)) {
            throw ValidationException("Insufficient actual balance", INSUFFICIENT_ACTUAL_BALANCE)
        }
    }

}