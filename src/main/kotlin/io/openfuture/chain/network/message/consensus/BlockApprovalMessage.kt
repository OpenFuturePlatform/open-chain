package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
data class BlockApprovalMessage(
    var stageId: Int,
    var hash: String,
    var publicKey: String,
    var signature: String? = null
) : Message {

    override fun read(buf: ByteBuf) {
        stageId = buf.readInt()
        hash = buf.readString()
        publicKey = buf.readString()
        signature = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeInt(stageId)
        buf.writeString(hash)
        buf.writeString(publicKey)
        buf.writeString(signature!!)
    }

    fun getBytes(): ByteArray = StringBuilder()
        .append(stageId)
        .append(hash)
        .append(publicKey)
        .toString().toByteArray()

}