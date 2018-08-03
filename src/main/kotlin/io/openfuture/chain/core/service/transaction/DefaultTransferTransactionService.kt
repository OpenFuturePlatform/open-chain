package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
internal class DefaultTransferTransactionService(
    private val repository: TransactionRepository<TransferTransaction>,
    private val uRepository: UTransactionRepository<UTransferTransaction>
) : BaseTransactionService(), TransferTransactionService {

    @Transactional
    override fun add(message: TransferTransactionMessage): UTransferTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UTransferTransaction.of(message)
        }

        val tx = UTransferTransaction.of(message)
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun add(request: TransferTransactionRequest): UTransferTransaction {
        val tx = request.toUEntity(clock.networkTime())
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun toBlock(utx: UTransferTransaction, block: MainBlock) {
        updateTransferBalance(utx.senderAddress, utx.payload.recipientAddress, utx.payload.amount)

        val tx = TransferTransaction.of(utx)
        tx.block = block
        updateBalanceByFee(tx)
        uRepository.delete(utx)
        repository.save(tx)
    }

    @Transactional
    fun validate(tx: UTransferTransaction) {
        if (!isValidTransferBalance(tx.senderAddress, tx.payload.amount + tx.payload.fee)) {
            throw ValidationException("There is not enough money on the wallet for transfer operation!")
        }
        super.validate(tx)
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