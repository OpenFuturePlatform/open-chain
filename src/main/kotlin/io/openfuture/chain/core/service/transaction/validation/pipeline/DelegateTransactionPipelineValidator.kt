package io.openfuture.chain.core.service.transaction.validation.pipeline

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_DELEGATE
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Scope(SCOPE_PROTOTYPE)
@Transactional(readOnly = true)
class DelegateTransactionPipelineValidator(
    private val consensusProperties: ConsensusProperties,
    private val uRepository: UDelegateTransactionRepository
) : TransactionPipelineValidator<DelegateTransactionPipelineValidator>() {

    fun check(): DelegateTransactionPipelineValidator {
        checkHash()
        checkSignature()
        checkSenderAddress()
        checkFeeDelegateTx()
        checkAmountDelegateTx()
        return this
    }

    fun checkNew(): DelegateTransactionPipelineValidator {
        check()
        checkActualBalance()
        checkDelegate()
        checkSendRequest()
        return this
    }

    fun checkFeeDelegateTx(): DelegateTransactionPipelineValidator {
        handlers.add {
            it as DelegateTransaction
            if (it.fee != consensusProperties.feeDelegateTx!!) {
                throw ValidationException("Fee should be ${consensusProperties.feeDelegateTx!!}")
            }
        }
        return this
    }

    fun checkAmountDelegateTx(): DelegateTransactionPipelineValidator {
        handlers.add {
            it as DelegateTransaction
            if (it.getPayload().amount != consensusProperties.amountDelegateTx!!) {
                throw ValidationException("Amount should be ${consensusProperties.amountDelegateTx!!}")
            }
        }
        return this
    }

    fun checkDelegate(): DelegateTransactionPipelineValidator {
        handlers.add {
            it as DelegateTransaction
            if (stateManager.isExistsDelegateByPublicKey(it.getPayload().delegateKey)) {
                throw ValidationException("Node ${it.getPayload().delegateKey} already registered as delegate",
                    ALREADY_DELEGATE)
            }
        }
        return this
    }

    fun checkSendRequest(): DelegateTransactionPipelineValidator {
        handlers.add { tx ->
            tx as DelegateTransaction
            BlockchainLock.readLock.lock()
            try {
                if (uRepository.findAll().any { it.getPayload().delegateKey == tx.getPayload().delegateKey }) {
                    throw ValidationException("Node ${tx.getPayload().delegateKey} already send request to become delegate",
                        ALREADY_DELEGATE)
                }
            } finally {
                BlockchainLock.readLock.unlock()
            }
        }
        return this
    }

}