package io.openfuture.chain.network.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.core.*

interface SyncBlockHandler {

    fun synchronize()

    fun getSyncStatus(): SynchronizationStatus

    fun getLastResponseTime(): Long

    fun onHashBlockRequestMessage(ctx: ChannelHandlerContext, message: HashBlockRequestMessage)

    fun onHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage)

    fun onSyncBlocRequestMessage(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage)

    fun onMainBlockMessage(block: MainBlockMessage)

    fun onGenesisBlockMessage(block: GenesisBlockMessage)

}
