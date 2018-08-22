package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INSUFFICIENT_BALANCE
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository,
    capacityChecker: TransactionCapacityChecker,
    private val networkService: NetworkApiService
) : ExternalTransactionService<TransferTransaction, UnconfirmedTransferTransaction>(repository, uRepository, capacityChecker), TransferTransactionService {

    companion object {
        val log = LoggerFactory.getLogger(DefaultTransferTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): TransferTransaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<TransferTransaction> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedTransferTransaction> = unconfirmedRepository.findAllByOrderByHeaderFeeDesc()

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getByAddress(address: String): List<TransferTransaction> {
        val senderTransactions = repository.findAllByHeaderSenderAddress(address)
        val recipientTransactions = (repository as TransferTransactionRepository).findAllByPayloadRecipientAddress(address)

        return senderTransactions + recipientTransactions
    }

    @Transactional
    override fun add(message: TransferTransactionMessage): UnconfirmedTransferTransaction {
        val persistUtx = unconfirmedRepository.findOneByHash(message.hash)

        if (null != persistUtx) {
            return persistUtx
        }

        val header = TransactionHeader(message.timestamp, message.fee, message.senderAddress)
        val payload = TransferTransactionPayload(message.amount, message.recipientAddress)

        validate(header, payload, message.hash, message.senderSignature, message.senderPublicKey)
        val utx = UnconfirmedTransferTransaction(header, message.hash, message.senderSignature, message.senderPublicKey, payload)
        val savedUtx = this.save(utx)
        networkService.broadcast(message)
        return savedUtx
    }

    @BlockchainSynchronized(throwable = true)
    @Transactional
    override fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction {
        val persistUtx = unconfirmedRepository.findOneByHash(request.hash!!)

        if (null != persistUtx) {
            return persistUtx
        }

        val header = TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!)
        val payload = TransferTransactionPayload(request.amount!!, request.recipientAddress!!)

        validate(header, payload, request.hash!!, request.senderSignature!!, request.senderPublicKey!!)
        val utx = UnconfirmedTransferTransaction(header, request.hash!!, request.senderSignature!!, request.senderPublicKey!!, payload)
        val savedUtx = this.save(utx)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    @Transactional
    override fun toBlock(message: TransferTransactionMessage, block: MainBlock): TransferTransaction {
        val tx = repository.findOneByHash(message.hash)
        if (null != tx) {
            return tx
        }

        val utx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != utx) {
            return confirm(utx, TransferTransaction.of(utx, block))
        }

        return this.save(TransferTransaction.of(message, block))
    }

    @Transactional
    override fun verify(message: TransferTransactionMessage): Boolean {
        return try {
            val header = TransactionHeader(message.timestamp, message.fee, message.senderAddress)
            val payload = TransferTransactionPayload(message.amount, message.recipientAddress)
            validate(header, payload, message.hash, message.senderSignature, message.senderPublicKey)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: TransferTransaction): TransferTransaction {
        updateTransferBalance(tx.header.senderAddress, tx.payload.recipientAddress, tx.payload.amount)
        return super.save(tx)
    }

    private fun validate(header: TransactionHeader, payload: TransferTransactionPayload, hash: String,
                         senderSignature: String, senderPublicKey: String) {
        if (!isValidBalance(header.senderAddress, payload.amount, header.fee)) {
            throw ValidationException("Insufficient balance", INSUFFICIENT_BALANCE)
        }

        super.validateExternal(header, payload, hash, senderSignature, senderPublicKey)
    }

    private fun updateTransferBalance(from: String, to: String, amount: Long) {
        walletService.increaseBalance(to, amount)
        walletService.decreaseBalance(from, amount)
    }

    private fun isValidBalance(address: String, amount: Long, fee: Long): Boolean {
        if (amount < 0 || fee < 0) {
            return false
        }

        val balance = walletService.getBalanceByAddress(address)
        val unconfirmedFee = baseService.getAllUnconfirmedByAddress(address).map { it.header.fee }.sum()
        val unconfirmedAmount = unconfirmedRepository.findAllByHeaderSenderAddress(address).map { it.payload.amount }.sum()
        val unspentBalance = balance - (unconfirmedFee + unconfirmedAmount)

        return unspentBalance >= amount + fee
    }

}