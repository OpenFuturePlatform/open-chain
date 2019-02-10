package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.*
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.service.*
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
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


    override fun validateNew(utx: UnconfirmedTransaction) {
        validate(utx)

        when (utx) {
            is UnconfirmedDelegateTransaction -> {
                checkActualBalance(utx.senderAddress, utx.getPayload().amount + utx.fee)
                delegateTransactionValidator.validateNew(utx)
            }
            is UnconfirmedTransferTransaction -> {
                checkActualBalance(utx.senderAddress, utx.getPayload().amount + utx.fee)
                transferTransactionValidator.validateNew(utx)
            }
            is UnconfirmedVoteTransaction -> {
                checkActualBalance(utx.senderAddress, utx.fee)
                voteTransactionValidator.validateNew(utx)
            }
        }
    }

    override fun verify(tx: BaseTransaction): Boolean {
        return try {
            validate(tx)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun validate(tx: BaseTransaction) {
        checkHash(tx)
        checkSignature(tx)

        if (tx is UnconfirmedTransaction) {
            checkSenderAddress(tx.senderAddress, tx.publicKey)

            when (tx) {
                is UnconfirmedDelegateTransaction -> delegateTransactionValidator.validate(tx)
                is UnconfirmedTransferTransaction -> transferTransactionValidator.validate(tx)
                is UnconfirmedVoteTransaction -> voteTransactionValidator.validate(tx)
            }
        }
    }

    private fun checkHash(tx: BaseTransaction) {
        if (ByteUtils.toHexString(HashUtils.doubleSha256(tx.getBytes())) != tx.hash) {
            throw ValidationException("Incorrect hash", INCORRECT_HASH)
        }
    }

    private fun checkSignature(tx: BaseTransaction) {
        if (!SignatureUtils.verify(ByteUtils.fromHexString(tx.hash), tx.signature, ByteUtils.fromHexString(tx.publicKey))) {
            throw ValidationException("Incorrect signature", INCORRECT_SIGNATURE)
        }
    }

    private fun checkSenderAddress(senderAddress: String, publicKey: String) {
        if (!cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(publicKey))) {
            throw ValidationException("Incorrect sender address", INCORRECT_ADDRESS)
        }
    }

    private fun checkActualBalance(address: String, amount: Long): Boolean {
        val balance = stateManager.getWalletBalanceByAddress(address)
        val unconfirmedBalance = transactionManager.getUnconfirmedBalanceBySenderAddress(address)

        return balance + unconfirmedBalance >= amount
    }

}