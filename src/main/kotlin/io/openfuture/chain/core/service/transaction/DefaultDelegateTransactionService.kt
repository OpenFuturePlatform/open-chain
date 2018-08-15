package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
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
        val utx = UnconfirmedDelegateTransaction.of(message)
        if (isExists(utx.hash)) {
            return utx
        }

        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = this.save(utx)
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction {
        val utx = UnconfirmedDelegateTransaction.of(request)
        if (isExists(utx.hash)) {
            return utx
        }

        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = this.save(utx)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    @Transactional
    override fun save(tx: DelegateTransaction): DelegateTransaction {
        delegateService.save(Delegate(tx.payload.delegateKey, tx.header.senderAddress, tx.payload.delegateHost, tx.payload.delegatePort))
        return super.save(tx)
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
    override fun isValid(message: DelegateTransactionMessage): Boolean {
        return isValid(UnconfirmedDelegateTransaction.of(message))
    }

    private fun isValid(utx: UnconfirmedDelegateTransaction): Boolean {
        return isNotExistsByDelegatePublicKey(utx.payload.delegateKey) && super.isValidBase(utx)
    }

    private fun isNotExistsByDelegatePublicKey(key: String): Boolean {
        return !delegateService.isExistsByPublicKey(key) && !unconfirmedRepository.findAll().any { it.payload.delegateKey == key }
    }

}