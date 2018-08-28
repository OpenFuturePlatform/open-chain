package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_HASH
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_SIGNATURE
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

abstract class BaseTransactionService {

    protected fun validateBase(header: TransactionHeader, payload: TransactionPayload, footer: TransactionFooter) {
        if (!isValidHash(header, payload, footer.hash)) {
            throw ValidationException("Incorrect hash", INCORRECT_HASH)
        }

        if (!isValidSignature(footer.hash, footer.senderSignature, footer.senderPublicKey)) {
            throw ValidationException("Incorrect signature", INCORRECT_SIGNATURE)
        }

        if (!isValidBalance(utx.header.senderAddress, amount)) {
            throw ValidationException("Insufficient balance", INSUFFICIENT_BALANCE)
        }
    }

    protected fun createHash(header: TransactionHeader, payload: TransactionPayload): String {
        val bytes = ByteBuffer.allocate(header.getBytes().size + payload.getBytes().size)
            .put(header.getBytes())
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    private fun isValidHash(header: TransactionHeader, payload: TransactionPayload, hash: String): Boolean {
        return createHash(header, payload) == hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean {
        return SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))
    }

    private fun isValidBalance(address: String, amount: Long): Boolean =
        walletService.getBalanceByAddress(address) >= amount

}