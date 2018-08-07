package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class BlockApprovalMessage(
    var stageId: Int,
    var hash: String,
    var publicKey: String,
    var signature: String? = null
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        stageId = buffer.readInt()
        hash = buffer.readString()
        publicKey = buffer.readString()
        signature = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeInt(stageId)
        buffer.writeString(hash)
        buffer.writeString(publicKey)
        buffer.writeString(signature!!)
    }

    fun getBytes(): ByteArray = StringBuilder()
        .append(stageId)
        .append(hash)
        .append(publicKey)
        .toString().toByteArray()

}