package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.network.message.core.TransferTransactionMessage
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
    capacityChecker: TransactionCapacityChecker
) : ExternalTransactionService<TransferTransaction, UnconfirmedTransferTransaction>(repository, uRepository, capacityChecker), TransferTransactionService {

    companion object {
        val log = LoggerFactory.getLogger(DefaultTransferTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): TransferTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<TransferTransaction> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedTransferTransaction> = unconfirmedRepository.findAllByOrderByHeaderFeeDesc()

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction = unconfirmedRepository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getByAddress(address: String): List<TransferTransaction> {
        val senderTransactions = repository.findAllByHeaderSenderAddress(address)
        val recipientTransactions = (repository as TransferTransactionRepository).findAllByPayloadRecipientAddress(address)

        return senderTransactions + recipientTransactions
    }

    @Transactional
    override fun add(message: TransferTransactionMessage): UnconfirmedTransferTransaction {
        return super.add(UnconfirmedTransferTransaction.of(message))
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction {
        return super.add(UnconfirmedTransferTransaction.of(request))
    }

    @Transactional
    override fun toBlock(message: TransferTransactionMessage, block: MainBlock): TransferTransaction {
        val tx = repository.findOneByFooterHash(message.hash)
        if (null != tx) {
            return tx
        }

        walletService.increaseBalance(tx!!.payload.recipientAddress, tx!!.payload.amount)
        walletService.decreaseBalance(tx!!.header.senderAddress, tx!!.payload.amount)

        val utx = unconfirmedRepository.findOneByFooterHash(message.hash)
        if (null != utx) {
            return confirm(utx, TransferTransaction.of(utx, block))
        }

        return this.save(TransferTransaction.of(message, block))
    }

    @Transactional
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
    override fun validate(utx: UnconfirmedTransferTransaction) {
        super.validateExternal(utx.header, utx.payload, utx.footer, utx.payload.amount + utx.header.fee)
    }

}