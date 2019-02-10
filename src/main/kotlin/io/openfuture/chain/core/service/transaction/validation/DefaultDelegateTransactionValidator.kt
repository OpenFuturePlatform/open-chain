package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_DELEGATE
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionValidator
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateTransactionValidator(
    private val consensusProperties: ConsensusProperties,
    private val stateManager: StateManager,
    private val uRepository: UDelegateTransactionRepository
) : DelegateTransactionValidator {

    override fun validateNew(utx: UnconfirmedDelegateTransaction) {
        checkDelegate(utx)
        checkSendRequest(utx)
    }

    override fun validate(utx: UnconfirmedDelegateTransaction) {
        checkFeeDelegateTx(utx)
        checkAmountDelegateTx(utx)
    }

    private fun checkFeeDelegateTx(utx: UnconfirmedDelegateTransaction) {
        if (utx.fee != consensusProperties.feeDelegateTx!!) {
            throw ValidationException("Fee should be ${consensusProperties.feeDelegateTx!!}")
        }
    }

    private fun checkAmountDelegateTx(utx: UnconfirmedDelegateTransaction) {
        if (utx.getPayload().amount != consensusProperties.amountDelegateTx!!) {
            throw ValidationException("Amount should be ${consensusProperties.amountDelegateTx!!}")
        }
    }

    private fun checkDelegate(utx: UnconfirmedDelegateTransaction) {
        if (stateManager.isExistsDelegateByPublicKey(utx.getPayload().delegateKey)) {
            throw ValidationException("Node ${utx.getPayload().delegateKey} already registered as delegate",
                ALREADY_DELEGATE)
        }
    }

    private fun checkSendRequest(utx: UnconfirmedDelegateTransaction) {
        BlockchainLock.readLock.lock()
        try {
            if (uRepository.findAll().any { it.getPayload().delegateKey == utx.getPayload().delegateKey }) {
                throw ValidationException("Node ${utx.getPayload().delegateKey} already send request to become delegate",
                    ALREADY_DELEGATE)
            }
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}