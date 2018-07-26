package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.Delegate

@NoArgConstructor
data class NetworkDelegate(
    var host: String,
    var port: Int,
    var rating: Int
) : NetworkEntity() {

    constructor(delegate: Delegate) : this(delegate.host, delegate.port, delegate.rating)

    override fun read(buffer: ByteBuf) {
        host = readString(buffer)
        port = buffer.readInt()
        rating = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        writeString(buffer, host)
        buffer.writeInt(port)
        buffer.writeInt(rating)
    }

}