package io.openfuture.chain.core.util

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

object TransactionUtils {


    fun generateHash(timestamp: Long, fee: Long, payload: TransactionPayload): String {
        val bytes = getBytes(timestamp, fee, payload)
        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    private fun getBytes(timestamp: Long, fee: Long, payload: TransactionPayload): ByteArray {
        return ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + payload.getBytes().size)
            .putLong(timestamp)
            .putLong(fee)
            .put(payload.getBytes())
            .array()
    }

}