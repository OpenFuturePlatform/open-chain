package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.OpenClass
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_HASH
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_SIGNATURE
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired

@OpenClass
abstract class BaseTransactionService {

    @Autowired
    private lateinit var transactionService: TransactionService


    protected fun validateBase(header: TransactionHeader, payload: TransactionPayload, footer: TransactionFooter) {
        if (!isValidHash(header, payload, footer.hash)) {
            throw ValidationException("Incorrect hash", INCORRECT_HASH)
        }

        if (!isValidSignature(footer.hash, footer.senderSignature, footer.senderPublicKey)) {
            throw ValidationException("Incorrect signature", INCORRECT_SIGNATURE)
        }
    }

    private fun isValidHash(header: TransactionHeader, payload: TransactionPayload, hash: String): Boolean =
        transactionService.createHash(header, payload) == hash

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

}