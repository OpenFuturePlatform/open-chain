package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class DelegateResponseMessage(
    var nodesInfo: List<NodeInfo>,
    var synchronizationSessionId: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        nodesInfo = buf.readList()
        synchronizationSessionId = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeList(nodesInfo)
        buf.writeString(synchronizationSessionId)
    }

}