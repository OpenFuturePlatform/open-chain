package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.consensus.model.dto.block.BlockApprovalMessage

@NoArgConstructor
data class NetworkBlockApprovalMessage(
    var stage: NetworkObserverStage,
    var height: Long,
    var hash: String,
    var publicKey: String
) : Packet() {

    constructor(message: BlockApprovalMessage) : this(
        NetworkObserverStage(message.stage),
        message.height,
        message.hash,
        message.publicKey
    )

    override fun readParams(buffer: ByteBuf) {
        stage = NetworkObserverStage::class.java.newInstance()
        stage.readParams(buffer)
        height = buffer.readLong()
        val hashLength = buffer.readInt()
        hash = buffer.readCharSequence(hashLength, Charsets.UTF_8).toString()
        val publicKeyLength = buffer.readInt()
        publicKey = buffer.readCharSequence(publicKeyLength, Charsets.UTF_8).toString()
    }

    override fun writeParams(buffer: ByteBuf) {
        stage.writeParams(buffer)
        buffer.writeLong(height)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, Charsets.UTF_8)
        buffer.writeInt(publicKey.length)
        buffer.writeCharSequence(publicKey, Charsets.UTF_8)
    }

}