package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    uRepository: UDelegateTransactionRepository,
    private val delegateService: DelegateService,
    private val networkService: NetworkApiService
) : BaseTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction>(repository, uRepository), DelegateTransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedDelegateTransaction> {
        return unconfirmedRepository.findAllByOrderByHeaderFeeDesc()
    }

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction {
        if (isExists(message.hash)) {
            return UnconfirmedDelegateTransaction.of(message)
        }

        val savedUtx = this.add(UnconfirmedDelegateTransaction.of(message))
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction {
        val uTransaction = UnconfirmedDelegateTransaction.of(request)
        if (isExists(uTransaction.hash)) {
            return uTransaction
        }

        val savedUtx = this.add(uTransaction)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    @Transactional
    override fun add(tx: DelegateTransaction): DelegateTransaction {
        delegateService.save(Delegate(tx.payload.delegateKey, tx.header.senderAddress))
        return super.add(tx)
    }

    @Transactional
    override fun synchronize(message: DelegateTransactionMessage, block: MainBlock) {
        val tx = repository.findOneByHash(message.hash)
        if (null != tx) {
            return
        }

        val utx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != utx) {
            toBlock(utx, DelegateTransaction.of(utx, block))
            return
        }
        this.add(DelegateTransaction.of(message, block))
    }

    override fun generateHash(request: DelegateTransactionHashRequest): String =
        TransactionUtils.generateHash(
            request.timestamp!!,
            request.fee!!,
            request.senderAddress!!,
            DelegateTransactionPayload(request.delegateKey!!)
        )

    @Transactional
    override fun toBlock(hash: String, block: MainBlock): DelegateTransaction {
        val utx = getUnconfirmedByHash(hash)
        return toBlock(utx, DelegateTransaction.of(utx, block))
    }

    @Transactional
    override fun isValid(tx: DelegateTransaction): Boolean {
        return isNotExistsByDelegatePublicKey(tx.payload.delegateKey) && super.isValidBase(tx)
    }

    @Transactional
    override fun isValid(utx: UnconfirmedDelegateTransaction): Boolean {
        return isNotExistsByDelegatePublicKey(utx.payload.delegateKey) && super.isValidBase(utx)
    }

    private fun isNotExistsByDelegatePublicKey(key: String): Boolean {
        return !delegateService.isExistsByPublicKey(key) && !unconfirmedRepository.findAll().any { it.payload.delegateKey == key }
    }

}