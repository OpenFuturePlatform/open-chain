package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.*
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.*
import io.openfuture.chain.core.service.*
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTransactionValidatorManager(
    private val cryptoService: CryptoService,
    private val delegateTransactionValidator: DelegateTransactionValidator,
    private val transferTransactionValidator: TransferTransactionValidator,
    private val voteTransactionValidator: VoteTransactionValidator,
    private val stateManager: StateManager,
    @Lazy private val transactionManager: TransactionManager
) : TransactionValidatorManager {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultTransactionValidatorManager::class.java)
    }


    override fun verify(tx: Transaction, new: Boolean): Boolean {
        return try {
            validate(tx, new)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    override fun validate(tx: Transaction, new: Boolean) {
        checkHash(tx)
        checkSignature(tx)

        if (new) {
            checkActualBalance(tx)
        }

        if (tx !is RewardTransaction) {
            checkSenderAddress(tx.senderAddress, tx.publicKey)
        }

        when (tx) {
            is DelegateTransaction -> delegateTransactionValidator.validate(tx, new)
            is TransferTransaction -> transferTransactionValidator.validate(tx, new)
            is VoteTransaction -> voteTransactionValidator.validate(tx, new)
        }
    }

    private fun checkHash(tx: BaseTransaction) {
        if (toHexString(HashUtils.doubleSha256(tx.getBytes())) != tx.hash) {
            throw ValidationException("Incorrect hash", INCORRECT_HASH)
        }
    }

    private fun checkSignature(tx: BaseTransaction) {
        if (!SignatureUtils.verify(fromHexString(tx.hash), tx.signature, fromHexString(tx.publicKey))) {
            throw ValidationException("Incorrect signature", INCORRECT_SIGNATURE)
        }
    }

    private fun checkActualBalance(tx: Transaction) {
        val balance = stateManager.getWalletBalanceByAddress(tx.senderAddress)
        val unconfirmedBalance = transactionManager.getUnconfirmedBalanceBySenderAddress(tx.senderAddress)
        val actualBalance = balance - unconfirmedBalance

        val result = when (tx) {
            is DelegateTransaction -> actualBalance >= tx.fee + tx.getPayload().amount
            is TransferTransaction -> actualBalance >= tx.fee + tx.getPayload().amount
            is VoteTransaction -> actualBalance >= tx.fee
            else -> true
        }

        if (!result) {
            throw ValidationException("Incorrect balance", INSUFFICIENT_ACTUAL_BALANCE)
        }
    }

    private fun checkSenderAddress(senderAddress: String, publicKey: String) {
        if (!cryptoService.isValidAddress(senderAddress, fromHexString(publicKey))) {
            throw ValidationException("Incorrect sender address", INCORRECT_ADDRESS)
        }
    }

}