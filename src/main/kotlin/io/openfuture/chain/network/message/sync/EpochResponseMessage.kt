package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
class EpochResponseMessage(
    var delegateKey: String,
    var isEpochExists: Boolean,
    var genesisBlock: GenesisBlockMessage?,
    var mainBlocks: List<MainBlockMessage>
) : Message {

    override fun read(buf: ByteBuf) {
        delegateKey = buf.readString()
        isEpochExists = buf.readBoolean()
        mainBlocks = buf.readList()
        if (isEpochExists) {
            genesisBlock = GenesisBlockMessage::class.java.getConstructor().newInstance()
            genesisBlock!!.read(buf)
        }
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(delegateKey)
        buf.writeBoolean(isEpochExists)
        buf.writeList(mainBlocks)
        if (isEpochExists) {
            genesisBlock!!.write(buf)
        }
    }

}