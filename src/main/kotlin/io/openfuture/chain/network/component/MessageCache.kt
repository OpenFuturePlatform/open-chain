package io.openfuture.chain.network.component

import com.github.benmanes.caffeine.cache.Caffeine
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MessageCache(
    consensusProperties: ConsensusProperties
) {

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(consensusProperties.getPeriod(), TimeUnit.MILLISECONDS)
        .build<String, String>()


    fun saveIfAbsent(message: ByteArray): Boolean {
        val hash = HashUtils.sha256(message)
        val hexHash = ByteUtils.toHexString(hash)
        val hasResult = (null != cache.getIfPresent(hexHash))
        if (!hasResult) {
            cache.put(hexHash, hexHash)
        }
        return hasResult
    }

    fun size(): Long = cache.estimatedSize()

}