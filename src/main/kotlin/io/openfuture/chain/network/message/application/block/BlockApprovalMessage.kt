package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class BlockApprovalMessage(
    var stageId: Short,
    var height: Long,
    var hash: String,
    var publicKey: String,
    var signature: String? = null
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        stageId = buffer.readShort()
        height = buffer.readLong()
        val hashLength = buffer.readInt()
        hash = buffer.readCharSequence(hashLength, Charsets.UTF_8).toString()
        val publicKeyLength = buffer.readInt()
        publicKey = buffer.readCharSequence(publicKeyLength, Charsets.UTF_8).toString()
        val signatureLength = buffer.readInt()
        signature = buffer.readCharSequence(signatureLength, Charsets.UTF_8).toString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeShort(stageId.toInt())
        buffer.writeLong(height)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, Charsets.UTF_8)
        buffer.writeInt(publicKey.length)
        buffer.writeCharSequence(publicKey, Charsets.UTF_8)
        buffer.writeInt(signature!!.length)
        buffer.writeCharSequence(signature, Charsets.UTF_8)
    }

    fun getBytes(): ByteArray {
        return StringBuilder()
            .append(stageId)
            .append(height)
            .append(hash)
            .append(publicKey)
            .toString().toByteArray()
    }

}