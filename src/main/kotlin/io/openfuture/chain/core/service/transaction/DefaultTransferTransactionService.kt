package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
internal class DefaultTransferTransactionService(
    private val repository: TransactionRepository<TransferTransaction>,
    private val uRepository: UTransactionRepository<UTransferTransaction>
) : BaseTransactionService(), TransferTransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UTransferTransaction> {
        return uRepository.findAll()
    }

    @Transactional
    override fun add(dto: TransferTransactionDto): UTransferTransaction {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return UTransferTransaction.of(dto)
        }

        val tx = UTransferTransaction.of(dto)
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
    override fun toBlock(hash: String, block: MainBlock) {
        val unconfirmedTx = getUnconfirmedByHash(hash)
        updateTransferBalance(unconfirmedTx.senderAddress, unconfirmedTx.getPayload().recipientAddress,
            unconfirmedTx.getPayload().amount)

        val tx = unconfirmedTx.toConfirmed()
        tx.block = block
        updateBalanceByFee(tx)
        uRepository.delete(unconfirmedTx)
        repository.save(tx)
    }

    @Transactional
    fun validate(tx: UTransferTransaction) {
        if (!isValidTransferBalance(tx.senderAddress, tx.getPayload().amount + tx.getPayload().fee)) {
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

    private fun getUnconfirmedByHash(hash: String): UTransferTransaction = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed vote transaction with hash: $hash not found")

}