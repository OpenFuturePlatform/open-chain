package io.openfuture.chain.core.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.core.*

interface SyncBlockHandler {

    fun sync()
    fun saveBlocks(block: GenesisBlockMessage)
    fun saveBlocks(block: MainBlockMessage)
    fun getBlocks(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage)
    fun blockHashRequest(ctx: ChannelHandlerContext, message: HashBlockRequestMessage)
    fun blockHashResponse(ctx: ChannelHandlerContext, message: HashBlockResponseMessage)

}
