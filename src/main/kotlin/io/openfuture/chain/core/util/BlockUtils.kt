package io.openfuture.chain.core.util

import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.util.HashUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

object BlockUtils {

    fun createHash(timestamp: Long, height: Long, previousHash: String, reward: Long, payload: BlockPayload): ByteArray {
        val bytes = getBytes(timestamp, height, previousHash, reward, payload)
        return HashUtils.doubleSha256(bytes)
    }

    private fun getBytes(timestamp: Long, height: Long, previousHash: String, reward: Long,
                         payload: BlockPayload): ByteArray {
        val bufferSize = LONG_BYTES + LONG_BYTES + previousHash.toByteArray(UTF_8).size + LONG_BYTES + payload.getBytes().size
        return ByteBuffer.allocate(bufferSize)
            .putLong(timestamp)
            .putLong(height)
            .put(previousHash.toByteArray(UTF_8))
            .putLong(reward)
            .put(payload.getBytes())
            .array()
    }

}