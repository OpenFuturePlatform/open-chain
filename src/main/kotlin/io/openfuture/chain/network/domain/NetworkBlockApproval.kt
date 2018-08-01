package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.annotation.NoArgConstructor

@NoArgConstructor
data class NetworkBlockApproval(
    var stageId: Int,
    var height: Long,
    var hash: String,
    var publicKey: String,
    var signature: String? = null
) : Packet() {

    fun getBytes(): ByteArray {
        return StringBuilder()
            .append(stageId)
            .append(height)
            .append(hash)
            .append(publicKey)
            .toString().toByteArray()
    }

    override fun readParams(buffer: ByteBuf) {
        stageId = buffer.readInt()
        height = buffer.readLong()
        val hashLength = buffer.readInt()
        hash = buffer.readCharSequence(hashLength, Charsets.UTF_8).toString()
        val publicKeyLength = buffer.readInt()
        publicKey = buffer.readCharSequence(publicKeyLength, Charsets.UTF_8).toString()
        val signatureLength = buffer.readInt()
        signature = buffer.readCharSequence(signatureLength, Charsets.UTF_8).toString()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeInt(stageId)
        buffer.writeLong(height)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, Charsets.UTF_8)
        buffer.writeInt(publicKey.length)
        buffer.writeCharSequence(publicKey, Charsets.UTF_8)
        buffer.writeInt(signature!!.length)
        buffer.writeCharSequence(signature, Charsets.UTF_8)
    }

}