package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_DELEGATE_KEY
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    uRepository: UDelegateTransactionRepository,
    capacityChecker: TransactionCapacityChecker,
    private val delegateService: DelegateService,
    private val nodeKeyHolder: NodeKeyHolder
) : ExternalTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction>(repository, uRepository, capacityChecker), DelegateTransactionService {

    companion object {
        val log = LoggerFactory.getLogger(DefaultDelegateTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): DelegateTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedDelegateTransaction> {
        return unconfirmedRepository.findAllByOrderByHeaderFeeDesc()
    }

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction = unconfirmedRepository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @BlockchainSynchronized
    @Transactional
    override fun add(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction {
        return super.add(UnconfirmedDelegateTransaction.of(message))
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction {
        return super.add(UnconfirmedDelegateTransaction.of(request))
    }

    @Transactional
    override fun toBlock(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction {
        val tx = repository.findOneByFooterHash(message.hash)
        if (null != tx) {
            return tx
        }

        walletService.decreaseBalance(message.senderAddress, message.amount)

        val utx = unconfirmedRepository.findOneByFooterHash(message.hash)
        if (null != utx) {
            return confirm(utx, DelegateTransaction.of(utx, block))
        }

        return this.save(DelegateTransaction.of(message, block))
    }

    @Transactional
    override fun verify(message: DelegateTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedDelegateTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: DelegateTransaction): DelegateTransaction {
        delegateService.save(Delegate(tx.footer.senderPublicKey, tx.payload.nodeId, tx.header.senderAddress,
            tx.payload.delegateHost, tx.payload.delegatePort, tx.header.timestamp))
        return super.save(tx)
    }

    @Transactional
    override fun validate(utx: UnconfirmedDelegateTransaction) {
        if (!isValidNodeId(utx.payload.nodeId, utx.footer.senderPublicKey)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }

        super.validateExternal(utx.header, utx.payload, utx.footer, utx.payload.amount + utx.header.fee)
    }

    private fun isValidNodeId(nodeId: String, publicKey: String): Boolean {
        if(nodeId != nodeKeyHolder.getUid(ByteUtils.fromHexString(publicKey))) {
            return false
        }

        if(delegateService.isExistsByPublicKey(publicKey) &&
            unconfirmedRepository.findAll().any { it.payload.nodeId == nodeId }) {
            return false
        }

        return true
    }

}