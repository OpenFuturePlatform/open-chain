package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository,
    capacityChecker: TransactionCapacityChecker,
    private val networkService: NetworkApiService
) : BaseTransactionService<TransferTransaction, UnconfirmedTransferTransaction>(repository, uRepository, capacityChecker), TransferTransactionService {

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<TransferTransaction> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedTransferTransaction> = unconfirmedRepository.findAllByOrderByFeeDesc()

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getByAddress(address: String): List<TransferTransaction> {
        val senderTransactions = repository.findAllBySenderAddress(address)
        val recipientTransactions = (repository as TransferTransactionRepository).findAllByPayloadRecipientAddress(address)

        return senderTransactions + recipientTransactions
    }

    @Transactional
    override fun add(message: TransferTransactionMessage): UnconfirmedTransferTransaction {
        if (isExists(message.hash)) {
            return UnconfirmedTransferTransaction.of(message)
        }

        val savedUtx = super.save(UnconfirmedTransferTransaction.of(message))
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction {
        val uTransaction = UnconfirmedTransferTransaction.of(request)
        if (isExists(uTransaction.hash)) {
            return uTransaction
        }

        val savedUtx = super.save(uTransaction)
        networkService.broadcast(TransferTransactionMessage(savedUtx))
        return savedUtx
    }

    override fun synchronize(message: TransferTransactionMessage, block: MainBlock) {
        val tx = repository.findOneByHash(message.hash)
        if (null != tx) {
            return
        }

        val utx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != utx) {
            confirm(utx, block)
            return
        }
        super.save(TransferTransaction.of(message, block))
    }

    override fun generateHash(request: TransferTransactionHashRequest): String =
        TransactionUtils.generateHash(
            request.timestamp!!,
            request.fee!!,
            request.senderAddress!!,
            TransferTransactionPayload(request.amount!!, request.recipientAddress!!)
        )

    @Transactional
    override fun toBlock(hash: String, block: MainBlock): TransferTransaction {
        val utx = getUnconfirmedByHash(hash)
        return confirm(utx, block)
    }

    @Transactional
    override fun isValid(utx: UnconfirmedTransferTransaction): Boolean =
        isValidTransferBalance(utx.senderAddress, utx.payload.amount + utx.fee) && super.isValid(utx)

    @Transactional
    override fun isValid(tx: TransferTransaction): Boolean =
        isValidTransferBalance(tx.senderAddress, tx.payload.amount + tx.fee) && super.isValid(tx)

    private fun confirm(utx: UnconfirmedTransferTransaction, block: MainBlock): TransferTransaction {
        updateTransferBalance(utx.senderAddress, utx.payload.recipientAddress, utx.payload.amount)
        return super.confirmProcess(utx, TransferTransaction.of(utx, block))
    }

    private fun updateTransferBalance(from: String, to: String, amount: Long) {
        walletService.increaseBalance(to, amount)
        walletService.decreaseBalance(from, amount)
    }

    private fun isValidTransferBalance(address: String, amount: Long): Boolean {
        val balance = walletService.getBalanceByAddress(address)
        val unspentBalance = balance - unconfirmedRepository.findAllBySenderAddress(address).map { it.payload.amount }.sum()
        return unspentBalance >= amount
    }

}