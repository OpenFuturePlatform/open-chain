package io.openfuture.chain.core.util

import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

object TransactionUtils {

    fun generateHash(header: TransactionHeader, payload: TransactionPayload): String {
        val bytes = getBytes(header, payload)
        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    private fun getBytes(header: TransactionHeader, payload: TransactionPayload): ByteArray {
        return ByteBuffer.allocate(header.getBytes().size + payload.getBytes().size)
            .put(header.getBytes())
            .put(payload.getBytes())
            .array()
    }

}