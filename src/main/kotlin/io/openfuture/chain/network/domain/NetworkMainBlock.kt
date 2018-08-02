package io.openfuture.chain.network.domain

import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

@NoArgConstructor
class NetworkMainBlock(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    var merkleHash: String,
    var transactions: MutableSet<BaseTransactionDto>
) : NetworkBlock(height, previousHash, timestamp, reward) {

    constructor(height: Long, previousHash: String, timestamp: Long, reward: Long, hash: String, publicKey: String,
                signature: String, merkleHash: String, transactions: MutableSet<BaseTransactionDto>) :
        this(height, previousHash, timestamp, reward, merkleHash, transactions) {
        this.hash = hash
        this.publicKey = publicKey
        this.signature = signature
    }

    fun sign(publicKey: String, privateKey: ByteArray) : NetworkMainBlock {
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
        builder.append(merkleHash)
        return builder.toString().toByteArray()
    }

    override fun toString() = "NetworkMainBlock(hash=$hash)"

}
