package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
data class NetworkDelegate(
    var host: String,
    var port: Int,
    var rating: Int
) : NetworkEntity() {

    constructor(delegate: Delegate) : this(delegate.host, delegate.port, delegate.rating)

    override fun read(buffer: ByteBuf) {
        host = buffer.readString()
        port = buffer.readInt()
        rating = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(host)
        buffer.writeInt(port)
        buffer.writeInt(rating)
    }

}