package io.openfuture.chain.core.service.receipt.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.service.ReceiptValidator
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefaultReceiptValidator : ReceiptValidator {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultReceiptValidator::class.java)
    }


    override fun verify(receipt: Receipt): Boolean {
        return try {
            validate(receipt)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun validate(receipt: Receipt) {
        checkHash(receipt)
    }

    private fun checkHash(receipt: Receipt) {
        if (receipt.hash != ByteUtils.toHexString(HashUtils.doubleSha256(receipt.getBytes()))) {
            throw ValidationException("Incorrect hash in state")
        }
    }

}