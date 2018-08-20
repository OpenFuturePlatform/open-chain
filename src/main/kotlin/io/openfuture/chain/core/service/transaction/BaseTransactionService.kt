package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

abstract class BaseTransactionService {

    protected fun createHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload): String {
        val bytes = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES +
            senderAddress.toByteArray(UTF_8).size + payload.getBytes().size)
            .putLong(timestamp)
            .putLong(fee)
            .put(senderAddress.toByteArray(UTF_8))
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}