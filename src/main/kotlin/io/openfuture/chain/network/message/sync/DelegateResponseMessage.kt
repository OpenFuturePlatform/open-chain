package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class DelegateResponseMessage(
    var addresses: List<NetworkAddress>,
    var synchronizationSessionId: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        addresses = buf.readList()
        synchronizationSessionId = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeList(addresses)
        buf.writeString(synchronizationSessionId)
    }

}