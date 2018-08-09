package io.openfuture.chain.core.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.core.*

interface SyncBlockHandler {

    fun synchronize()

    fun isSynchronize(): Boolean

    fun handleHashBlockRequestMessage(ctx: ChannelHandlerContext, message: HashBlockRequestMessage)

    fun handleHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage)

    fun handleSyncBlocKRequestMessage(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage)

    fun handleMainBlockMessage(block: MainBlockMessage)

    fun handleGenesisBlockMessage(block: GenesisBlockMessage)


}
