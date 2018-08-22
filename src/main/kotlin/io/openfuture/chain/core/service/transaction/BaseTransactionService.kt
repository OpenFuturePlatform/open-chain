package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

abstract class BaseTransactionService {

    protected fun validateBase(header: TransactionHeader, payload: TransactionPayload, hash: String,
                               senderSignature: String, senderPublicKey: String) {
        if (!isValidHash(header, payload, hash)) {
            throw ValidationException("Incorrect hash", ExceptionType.INCORRECT_HASH)
        }

        if (!isValidSignature(hash, senderSignature, senderPublicKey)) {
            throw ValidationException("Incorrect signature", ExceptionType.INCORRECT_SIGNATURE)
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

}