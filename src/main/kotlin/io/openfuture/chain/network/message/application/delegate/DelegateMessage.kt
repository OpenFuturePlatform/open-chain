package io.openfuture.chain.network.message.application.delegate

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class DelegateMessage(
    var publicKey: String,
    var address: String
) : BaseMessage {

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