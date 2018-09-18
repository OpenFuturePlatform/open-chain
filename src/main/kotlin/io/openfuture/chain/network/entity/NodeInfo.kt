package io.openfuture.chain.network.entity

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class NodeInfo(
    var uid: String,
    var address: NetworkAddress
) : Serializable {

    override fun read(buf: ByteBuf) {
        uid = buf.readString()
        address = NetworkAddress::class.java.newInstance()
        address.read(buf)
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(uid)
        address.write(buf)
    }

}