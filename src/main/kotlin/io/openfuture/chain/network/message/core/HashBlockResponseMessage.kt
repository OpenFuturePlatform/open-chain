package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class HashBlockResponseMessage(
    hash: String,
    var synchronizationSessionId: String
): HashMessage(hash) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        synchronizationSessionId = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeString(synchronizationSessionId)
    }

}