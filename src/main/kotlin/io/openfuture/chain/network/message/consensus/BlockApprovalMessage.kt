package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage
import java.nio.charset.StandardCharsets.UTF_8

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
        hash = buffer.readCharSequence(hashLength, UTF_8).toString()
        val publicKeyLength = buffer.readInt()
        publicKey = buffer.readCharSequence(publicKeyLength, UTF_8).toString()
        val signatureLength = buffer.readInt()
        signature = buffer.readCharSequence(signatureLength, UTF_8).toString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeShort(stageId.toInt())
        buffer.writeLong(height)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, UTF_8)
        buffer.writeInt(publicKey.length)
        buffer.writeCharSequence(publicKey, UTF_8)
        buffer.writeInt(signature!!.length)
        buffer.writeCharSequence(signature, UTF_8)
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