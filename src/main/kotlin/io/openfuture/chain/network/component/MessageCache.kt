package io.openfuture.chain.network.component

import com.github.benmanes.caffeine.cache.Caffeine
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.property.NodeProperties
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MessageCache(
    nodeProperties: NodeProperties
) {

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(nodeProperties.messageCacheExpireInterval!!, TimeUnit.MILLISECONDS)
        .build<String, String>()


    fun getAndSaveHash(message: ByteArray): String? {
        val hash = HashUtils.sha256(message)
        val hexHash = ByteUtils.toHexString(hash)
        val result = cache.getIfPresent(hexHash)
        if (result == null) {
            cache.put(hexHash, hexHash)
        }
        return result
    }

    fun size(): Long = cache.estimatedSize()

}