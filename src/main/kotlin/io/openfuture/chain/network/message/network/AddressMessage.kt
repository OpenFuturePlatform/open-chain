package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class AddressMessage(
    var uid: String,
    var address: NetworkAddressMessage
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        uid = buffer.readString()
        address = NetworkAddressMessage::class.java.newInstance()
        address.read(buffer)
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(uid)
        address.write(buffer)
    }

}