package io.openfuture.chain.tendermint.handler

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransaction
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransferTransaction
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.util.TendermintTransactionValidateHandler
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class TendermintTransactionValidator {

    @Autowired
    private lateinit var cryptoService: CryptoService
    @Autowired
    protected lateinit var stateManager: StateManager
    companion object {
        private val log: Logger = LoggerFactory.getLogger(TendermintTransactionValidator::class.java)
    }
    fun verify(tx: TendermintTransaction, transactionValidationPipeline: TendermintTransactionValidationPipeline): Boolean {
        println("start verifying")
        return try {
            validate(tx, transactionValidationPipeline)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    fun validate(tx: TendermintTransaction, transactionValidationPipeline: TendermintTransactionValidationPipeline) {
        println("start validating")
        transactionValidationPipeline.invoke(tx)
    }

    fun checkHash(): TendermintTransactionValidateHandler = {
        println("CHECKING HASH")
        if (ByteUtils.toHexString(HashUtils.doubleSha256(it.getBytes())) != it.hash) {
            throw ValidationException("Incorrect hash", ExceptionType.INCORRECT_HASH)
        }
    }

    fun checkSignature(): TendermintTransactionValidateHandler = {
        println("CHECKING SIGNATURE")
        if (!SignatureUtils.verify(
                ByteUtils.fromHexString(it.hash), it.signature,
                ByteUtils.fromHexString(it.publicKey)
            )) {
            throw ValidationException("Incorrect signature", ExceptionType.INCORRECT_SIGNATURE)
        }
    }

    fun checkActualBalance(unconfirmedBalance: Long): TendermintTransactionValidateHandler = {
        val balance = stateManager.getWalletBalanceByAddress(it.senderAddress)
        val actualBalance = balance - unconfirmedBalance

        val result = when (it) {
            is TendermintTransferTransaction -> actualBalance >= it.fee + it.getPayload().amount
            else -> true
        }

        if (!result) {
            throw ValidationException("Incorrect balance", ExceptionType.INSUFFICIENT_ACTUAL_BALANCE)
        }
    }

    fun checkSenderAddress(): TendermintTransactionValidateHandler = {
        println("CHECKING SENDER ADDRESS")
        if (!cryptoService.isValidAddress(it.senderAddress, ByteUtils.fromHexString(it.publicKey))) {
            throw ValidationException("Incorrect sender address", ExceptionType.INCORRECT_ADDRESS)
        }
    }
}