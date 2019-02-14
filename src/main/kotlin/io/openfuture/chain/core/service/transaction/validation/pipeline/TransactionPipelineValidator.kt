package io.openfuture.chain.core.service.transaction.validation.pipeline

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.*
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.util.TransactionValidateHandler
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class TransactionPipelineValidator<T : TransactionPipelineValidator<T>> {

    @Lazy @Autowired private lateinit var transactionManager: TransactionManager
    @Autowired private lateinit var cryptoService: CryptoService
    @Autowired protected lateinit var stateManager: StateManager

    protected val handlers = mutableListOf<TransactionValidateHandler>()

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TransactionPipelineValidator::class.java)
    }


    fun verify(tx: Transaction): Boolean {
        return try {
            validate(tx)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    fun validate(tx: Transaction) {
        handlers.forEach { it.invoke(tx) }
        handlers.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun checkHash(): T {
        handlers.add {
            if (toHexString(HashUtils.doubleSha256(it.getBytes())) != it.hash) {
                throw ValidationException("Incorrect hash", INCORRECT_HASH)
            }
        }
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun checkSignature(): T {
        handlers.add {
            if (!SignatureUtils.verify(fromHexString(it.hash), it.signature, fromHexString(it.publicKey))) {
                throw ValidationException("Incorrect signature", INCORRECT_SIGNATURE)
            }
        }
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun checkActualBalance(): T {
        handlers.add {
            val balance = stateManager.getWalletBalanceByAddress(it.senderAddress)
            val unconfirmedBalance = transactionManager.getUnconfirmedBalanceBySenderAddress(it.senderAddress)
            val actualBalance = balance - unconfirmedBalance

            val result = when (it) {
                is DelegateTransaction -> actualBalance >= it.fee + it.getPayload().amount
                is TransferTransaction -> actualBalance >= it.fee + it.getPayload().amount
                is VoteTransaction -> actualBalance >= it.fee
                else -> true
            }

            if (!result) {
                throw ValidationException("Incorrect balance", INSUFFICIENT_ACTUAL_BALANCE)
            }
        }
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun checkSenderAddress(): T {
        handlers.add {
            if (!cryptoService.isValidAddress(it.senderAddress, fromHexString(it.publicKey))) {
                throw ValidationException("Incorrect sender address", INCORRECT_ADDRESS)
            }
        }
        return this as T
    }

}