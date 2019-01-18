package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
class DelegateMessage(
    var publicKey: String,
    var nodeId: String,
    var address: String,
    var host: String,
    var port: Int,
    var registrationDate: Long
): Serializable {

    override fun read(buf: ByteBuf) {
        publicKey = buf.readString()
        nodeId = buf.readString()
        address = buf.readString()
        host = buf.readString()
        port = buf.readInt()
        registrationDate = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(publicKey)
        buf.writeString(nodeId)
        buf.writeString(address)
        buf.writeString(host)
        buf.writeInt(port)
        buf.writeLong(registrationDate)
    }

}