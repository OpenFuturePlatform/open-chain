package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.OpenClass
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_HASH
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_SIGNATURE
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString

@OpenClass
abstract class BaseTransactionService {

    protected fun validateBase(transaction: BaseTransaction) {
        if (!isValidHash(transaction)) {
            throw ValidationException("Incorrect hash", INCORRECT_HASH)
        }

        if (!isValidSignature(transaction)) {
            throw ValidationException("Incorrect signature", INCORRECT_SIGNATURE)
        }
    }

    private fun isValidHash(transaction: BaseTransaction): Boolean =
        ByteUtils.toHexString(HashUtils.sha256(transaction.getBytes())) == transaction.hash

    private fun isValidSignature(transaction: BaseTransaction): Boolean =
        SignatureUtils.verify(fromHexString(transaction.hash), transaction.signature, fromHexString(transaction.publicKey))

}