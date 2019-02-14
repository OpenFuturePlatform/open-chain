package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message
import io.openfuture.chain.network.message.sync.GenesisBlockMessage

@NoArgConstructor
class BlockAvailabilityResponse(
    var hash: String,
    var height: Long,
    var genesisBlock: GenesisBlockMessage? = null
): Message {

    override fun read(buf: ByteBuf) {
        hash = buf.readString()
        height = buf.readLong()
        if (-1L != height) {
            val block = GenesisBlockMessage::class.java.newInstance()
            block.read(buf)
            genesisBlock = block
        }
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(hash)
        buf.writeLong(height)
        genesisBlock?.write(buf)
    }

}