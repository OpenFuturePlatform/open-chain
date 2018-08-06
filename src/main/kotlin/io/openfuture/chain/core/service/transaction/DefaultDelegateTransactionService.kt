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
import io.openfuture.chain.network.service.NetworkService
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    uRepository: UDelegateTransactionRepository,
    private val delegateService: DelegateService,
    private val networkService: NetworkService
) : BaseTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction>(repository, uRepository), DelegateTransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedDelegateTransaction> {
        return unconfirmedRepository.findAllByOrderByFeeDesc()
    }

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction {
        val persistTx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != persistTx) {
            return UnconfirmedDelegateTransaction.of(message)
        }

        val savedUtx = super.save(UnconfirmedDelegateTransaction.of(message))
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction {
        val savedUtx = super.save(UnconfirmedDelegateTransaction.of(clock.networkTime(), request))
        networkService.broadcast(DelegateTransactionMessage(savedUtx))
        return savedUtx
    }

    @Transactional
    override fun synchronize(message: DelegateTransactionMessage, block: MainBlock) {
        val persistTx = repository.findOneByHash(message.hash)
        if (null != persistTx) {
            return
        }

        val persistUtx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != persistUtx) {
            toBlock(persistUtx.hash, block)
            return
        }
        super.save(DelegateTransaction.of(message))
    }

    override fun generateHash(request: DelegateTransactionHashRequest): String {
        return TransactionUtils.generateHash(request.timestamp!!, request.fee!!, request.senderAddress!!,
            DelegateTransactionPayload(request.delegateKey!!))
    }

    @Transactional
    override fun toBlock(hash: String, block: MainBlock): DelegateTransaction {
        val utx = getUnconfirmedByHash(hash)
        delegateService.save(Delegate(utx.payload.delegateKey, utx.senderAddress))
        return super.toBlock(utx, DelegateTransaction.of(utx), block)
    }

    @Transactional
    override fun isValid(tx: DelegateTransaction): Boolean {
        return isNotExistDelegate(tx.senderAddress) && super.isValid(tx)
    }

    @Transactional
    override fun isValid(utx: UnconfirmedDelegateTransaction): Boolean {
        return isNotExistDelegate(utx.senderAddress) && super.isValid(utx)
    }

    private fun isNotExistDelegate(key: String): Boolean {
        return !delegateService.isExists(key)
    }

}