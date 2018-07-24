package io.openfuture.chain.util

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.HashUtils.doubleSha256
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.transaction.BaseTransaction
import java.util.*

object BlockUtils {

    fun calculateMerkleRoot(transactions: Set<BaseTransaction>): String {
        if (transactions.size == 1) {
            return transactions.single().hash
        }
        var previousTreeLayout = transactions.map { it.hash.toByteArray() }
        var treeLayout = mutableListOf<ByteArray>()
        while(previousTreeLayout.size != 2) {
            for (i in 0 until previousTreeLayout.size step 2) {
                val leftHash = previousTreeLayout[i]
                val rightHash = if (i + 1 == previousTreeLayout.size) {
                    previousTreeLayout[i]
                } else {
                    previousTreeLayout[i + 1]
                }
                treeLayout.add(HashUtils.sha256(leftHash + rightHash))
            }
            previousTreeLayout = treeLayout
            treeLayout = mutableListOf()
        }
        return HashUtils.toHexString(doubleSha256(previousTreeLayout[0] + previousTreeLayout[1]))
    }

    fun calculateDelegatesHash(activeDelegates: Set<Delegate>): String {
        val delegatesContent = StringBuilder()
        for (activeDelegate in activeDelegates) {
            delegatesContent.append(activeDelegate.host)
            delegatesContent.append(activeDelegate.port)
            delegatesContent.append(activeDelegate.publicKey)
            delegatesContent.append(activeDelegate.rating)
        }
        val hash = HashUtils.doubleSha256(delegatesContent.toString().toByteArray(Charsets.UTF_8))
        return HashUtils.toHexString(hash)
    }

    fun calculateHash(previousHash: String, timestamp: Long, height: Long, merkleRoot: String = ""): ByteArray {
        val headers = previousHash + merkleRoot + timestamp + height
        return HashUtils.doubleSha256(headers.toByteArray())
    }

    fun getBlockProducer(delegates: Set<Delegate>, previousBlock: Block?): Delegate {
        val blockTimestamp = previousBlock?.timestamp ?: 0
        val random = Random(blockTimestamp)
        return delegates.shuffled(random).first()
    }

}