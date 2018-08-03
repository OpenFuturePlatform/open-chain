package io.openfuture.chain.core.util

import io.openfuture.chain.core.model.entity.block.payload.BaseBlockPayload
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

object BlockUtils {

    fun createHash(timestamp: Long, height: Long, previousHash: String, reward: Long, payload: BaseBlockPayload): ByteArray {
        val bytes = getBytes(timestamp, height, previousHash, reward, payload)
        return HashUtils.doubleSha256(bytes)
    }

    private fun getBytes(timestamp: Long, height: Long, previousHash: String, reward: Long,
                         payload: BaseBlockPayload): ByteArray {
        val builder = StringBuilder()
        builder.append(timestamp)
        builder.append(height)
        builder.append(previousHash)
        builder.append(reward)
        builder.append(payload)
        return builder.toString().toByteArray()
    }


}