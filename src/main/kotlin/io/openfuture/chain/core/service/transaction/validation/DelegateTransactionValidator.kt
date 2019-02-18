package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_DELEGATE
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.core.util.TransactionValidateHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DelegateTransactionValidator(
    private val consensusProperties: ConsensusProperties,
    private val uRepository: UDelegateTransactionRepository
) : TransactionValidator() {

    fun check(): Array<TransactionValidateHandler> = arrayOf(
        checkHash(),
        checkSignature(),
        checkSenderAddress(),
        checkFeeDelegateTx(),
        checkAmountDelegateTx()
    )

    fun checkNew(unconfirmedBalance: Long): Array<TransactionValidateHandler> = arrayOf(
        *check(),
        checkActualBalance(unconfirmedBalance),
        checkDelegate(),
        checkSendRequest()
    )

    fun checkFeeDelegateTx(): TransactionValidateHandler = {
        it as DelegateTransaction
        if (it.fee != consensusProperties.feeDelegateTx!!) {
            throw ValidationException("Fee should be ${consensusProperties.feeDelegateTx!!}")
        }
    }

    fun checkAmountDelegateTx(): TransactionValidateHandler = {
        it as DelegateTransaction
        if (it.getPayload().amount != consensusProperties.amountDelegateTx!!) {
            throw ValidationException("Amount should be ${consensusProperties.amountDelegateTx!!}")
        }
    }

    fun checkDelegate(): TransactionValidateHandler = {
        it as DelegateTransaction
        if (stateManager.isExistsDelegateByPublicKey(it.getPayload().delegateKey)) {
            throw ValidationException("Node ${it.getPayload().delegateKey} already registered as delegate",
                ALREADY_DELEGATE)
        }
    }

    fun checkSendRequest(): TransactionValidateHandler = { tx ->
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

}