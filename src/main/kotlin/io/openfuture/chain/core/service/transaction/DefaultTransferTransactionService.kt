package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.service.NetworkService
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
class DefaultTransferTransactionService(
    repository: TransactionRepository<TransferTransaction>,
    uRepository: UTransactionRepository<UTransferTransaction>,
    private val networkService: NetworkService
) : BaseTransactionService<TransferTransaction, UTransferTransaction>(repository, uRepository), TransferTransactionService {

    @Transactional
    override fun add(message: TransferTransactionMessage): UTransferTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UTransferTransaction.of(message)
        }

        val utx = UTransferTransaction.of(message)
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = super.add(utx)
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: TransferTransactionRequest): UTransferTransaction {
        val utx = UTransferTransaction.of(clock.networkTime(), request)
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = super.add(utx)
        networkService.broadcast(TransferTransactionMessage(savedUtx))
        return savedUtx
    }

    override fun generateHash(request: TransferTransactionHashRequest): String {
        return TransactionUtils.generateHash(request.timestamp!!, request.fee!!,
            TransferTransactionPayload(request.amount!!, request.recipientAddress!!))
    }

    @Transactional
    override fun toBlock(utx: UTransferTransaction, block: MainBlock): TransferTransaction {
        updateTransferBalance(utx.senderAddress, utx.payload.recipientAddress, utx.payload.amount)
        return super.toBlock(utx, TransferTransaction.of(utx), block)
    }

    private fun isValid(utx: UTransferTransaction): Boolean {
        return isValidTransferBalance(utx.senderAddress, utx.payload.amount + utx.fee) && super.isValid(utx)
    }

    private fun updateTransferBalance(from: String, to: String, amount: Long) {
        walletService.increaseBalance(to, amount)
        walletService.decreaseBalance(from, amount)
    }

    private fun isValidTransferBalance(address: String, amount: Long): Boolean {
        val unspentBalance = walletService.getUnspentBalanceByAddress(address)
        if (unspentBalance < amount) {
            return false
        }
        return true
    }

}