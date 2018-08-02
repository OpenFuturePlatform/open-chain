package io.openfuture.chain.network.domain

import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

@NoArgConstructor
class NetworkGenesisBlock(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    var epochIndex: Long,
    var activeDelegates: MutableSet<NetworkDelegate>
) : NetworkBlock(height, previousHash, timestamp, reward) {

    constructor(height: Long, previousHash: String, timestamp: Long, reward: Long, hash: String, publicKey: String,
                signature: String, epochIndex: Long, activeDelegates: MutableSet<NetworkDelegate>) :
        this(height, previousHash, timestamp, reward, epochIndex, activeDelegates) {
        this.hash = hash
        this.publicKey = publicKey
        this.signature = signature
    }

    fun sign(publicKey: String, privateKey: ByteArray) : NetworkGenesisBlock {
        this.publicKey = publicKey
        this.hash = ByteUtils.toHexString(HashUtils.doubleSha256((getBytes())))
        this.signature = SignatureUtils.sign(getBytes(), privateKey)
        return this
    }

    override fun getBytes() : ByteArray {
        val builder = StringBuilder()
        builder.append(height)
        builder.append(previousHash)
        builder.append(time)
        builder.append(reward)
        builder.append(epochIndex)
        builder.append(activeDelegates)
        return builder.toString().toByteArray()
    }

    override fun toString() = "NetworkGenesisBlock(hash=$hash)"

}
