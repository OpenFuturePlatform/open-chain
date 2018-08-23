package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_DELEGATE_KEY
import io.openfuture.chain.core.exception.model.ExceptionType.INSUFFICIENT_BALANCE
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    uRepository: UDelegateTransactionRepository,
    capacityChecker: TransactionCapacityChecker,
    private val delegateService: DelegateService,
    private val networkService: NetworkApiService
) : ExternalTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction>(repository, uRepository, capacityChecker), DelegateTransactionService {

    companion object {
        val log = LoggerFactory.getLogger(DefaultDelegateTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): DelegateTransaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedDelegateTransaction> {
        return unconfirmedRepository.findAllByOrderByHeaderFeeDesc()
    }

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction {
        val persistUtx = unconfirmedRepository.findOneByHash(message.hash)

        if (null != persistUtx) {
            return persistUtx
        }

        val header = TransactionHeader(message.timestamp, message.fee, message.senderAddress)
        val payload = DelegateTransactionPayload(message.delegateKey, message.delegateHost, message.delegatePort)

        validate(header, payload, message.hash, message.senderSignature, message.senderPublicKey)
        val utx = UnconfirmedDelegateTransaction(header, message.hash, message.senderSignature, message.senderPublicKey, payload)
        val savedUtx = this.save(utx)
        networkService.broadcast(message)
        return savedUtx
    }

    @BlockchainSynchronized(throwable = true)
    @Transactional
    override fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction {
        val persistUtx = unconfirmedRepository.findOneByHash(request.hash!!)

        if (null != persistUtx) {
            return persistUtx
        }

        val header = TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!)
        val payload = DelegateTransactionPayload(request.delegateKey!!, request.senderHost!!, request.senderPort!!)

        validate(header, payload, request.hash!!, request.senderSignature!!, request.senderPublicKey!!)
        val utx = UnconfirmedDelegateTransaction(header, request.hash!!, request.senderSignature!!, request.senderPublicKey!!, payload)
        val savedUtx = this.save(utx)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    @Transactional
    override fun toBlock(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction {
        val tx = repository.findOneByHash(message.hash)
        if (null != tx) {
            return tx
        }

        val utx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != utx) {
            return confirm(utx, DelegateTransaction.of(utx, block))
        }

        return this.save(DelegateTransaction.of(message, block))
    }

    @Transactional
    override fun verify(message: DelegateTransactionMessage): Boolean {
        try {
            val header = TransactionHeader(message.timestamp, message.fee, message.senderAddress)
            val payload = DelegateTransactionPayload(message.delegateKey, message.delegateHost, message.delegatePort)
            validate(header, payload, message.hash, message.senderSignature, message.senderPublicKey)
            return true
        } catch (e: ValidationException) {
            log.warn(e.message)
            return false
        }
    }

    override fun save(tx: DelegateTransaction): DelegateTransaction {
        delegateService.save(Delegate(tx.payload.delegateKey, tx.header.senderAddress, tx.payload.delegateHost, tx.payload.delegatePort))
        return super.save(tx)
    }

    private fun validate(header: TransactionHeader, payload: DelegateTransactionPayload, hash: String,
                         senderSignature: String, senderPublicKey: String) {
        if (!isNotExistsByDelegatePublicKey(payload.delegateKey)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }

        if (!isValidFee(header.senderAddress, header.fee)) {
            throw ValidationException("Insufficient balance", INSUFFICIENT_BALANCE)
        }

        super.validateExternal(header, payload, hash, senderSignature, senderPublicKey)
    }

    private fun isNotExistsByDelegatePublicKey(key: String): Boolean {
        return !delegateService.isExistsByPublicKey(key) && !unconfirmedRepository.findAll().any { it.payload.delegateKey == key }
    }

    private fun isValidFee(senderAddress: String, fee: Long): Boolean {
        val balance = walletService.getBalanceByAddress(senderAddress)
        val unspentBalance = balance - baseService.getAllUnconfirmedByAddress(senderAddress).map { it.header.fee }.sum()
        return fee in 0..unspentBalance
    }

}