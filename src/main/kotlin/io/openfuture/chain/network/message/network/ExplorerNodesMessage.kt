package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readStringList
import io.openfuture.chain.network.extension.writeStringList
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class ExplorerNodesMessage(
    var values: List<String>
) : Serializable {

    override fun read(buf: ByteBuf) {
        values = buf.readStringList()
    }

    override fun write(buf: ByteBuf) {
        buf.writeStringList(values)
    }

}