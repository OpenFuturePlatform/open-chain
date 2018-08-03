package io.openfuture.chain.core.util

import io.openfuture.chain.core.model.entity.block.payload.BaseBlockPayload
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

object BlockUtils {

    fun createHash(payload: BaseBlockPayload, publicKey: ByteArray, signature: String): String {
        val bytes = getBytes(publicKey, signature.toByteArray(), payload.getBytes())
        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    private fun getBytes(publicKey: ByteArray, signature: ByteArray, data: ByteArray): ByteArray {
        return ByteBuffer.allocate(data.size + publicKey.size + signature.size)
            .put(data)
            .put(publicKey)
            .put(signature)
            .array()
    }

}