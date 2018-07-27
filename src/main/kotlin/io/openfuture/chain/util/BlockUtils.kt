package io.openfuture.chain.util

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Delegate
import java.util.*

object BlockUtils {

    fun calculateDelegatesHash(activeDelegates: Set<Delegate>): String {
        val delegatesContent = StringBuilder()
        for (activeDelegate in activeDelegates) {
            delegatesContent.append(activeDelegate.publicKey)
            delegatesContent.append(activeDelegate.address)
        }
        val hash = HashUtils.doubleSha256(delegatesContent.toString().toByteArray(Charsets.UTF_8))
        return HashUtils.toHexString(hash)
    }

    fun getBlockProducer(delegates: Set<Delegate>, previousBlock: Block): Delegate {
        val blockTimestamp = previousBlock.timestamp
        val random = Random(blockTimestamp)
        return delegates.shuffled(random).first()
    }

}