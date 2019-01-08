package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class SyncResponseMessage(
    var genesisBlockMessage: GenesisBlockMessage
) : Serializable {

    override fun read(buf: ByteBuf) {
        genesisBlockMessage = GenesisBlockMessage::class.java.getConstructor().newInstance()
        genesisBlockMessage.read(buf)
    }

    override fun write(buf: ByteBuf) {
        genesisBlockMessage.write(buf)
    }

}