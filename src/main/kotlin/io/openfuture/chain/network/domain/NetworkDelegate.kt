package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
data class NetworkDelegate(
    var publicKey: String,
    var address: String
) : NetworkEntity() {

    constructor(delegate: Delegate) : this(delegate.publicKey, delegate.address)

    override fun read(buffer: ByteBuf) {
        publicKey = buffer.readString()
        address = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(publicKey)
        buffer.writeString(address)
    }

}