package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
class SyncRequestMessage : Serializable {

    override fun read(buf: ByteBuf) {

    }

    override fun write(buf: ByteBuf) {

    }

}