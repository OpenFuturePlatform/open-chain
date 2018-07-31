package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.component.block.ObserverStage

data class NetworkObserverStage(
    var value: Int
) {

    constructor(stage: ObserverStage) : this(stage.value)

    fun readParams(buffer: ByteBuf) {
        value = buffer.readInt()
    }

    fun writeParams(buffer: ByteBuf) {
        buffer.writeInt(value)
    }

}