package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.extension.readSet
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeSet
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
data class GreetingResponseMessage(
    var uid: String,
    var externalHost: String,
    var nodesInfo: Set<NodeInfo>,
    var accepted: Boolean = true,
    var loop: Boolean = false
) : Message {

    override fun read(buf: ByteBuf) {
        uid = buf.readString()
        externalHost = buf.readString()
        nodesInfo = buf.readSet()
        accepted = buf.readBoolean()
        loop = buf.readBoolean()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(uid)
        buf.writeString(externalHost)
        buf.writeSet(nodesInfo)
        buf.writeBoolean(accepted)
        buf.writeBoolean(loop)
    }

}