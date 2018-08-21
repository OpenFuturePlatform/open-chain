package io.openfuture.chain.network.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.core.*

interface SyncBlockResponseHandler {

    fun synchronize()

    fun getLastResponseTime(): Long

    fun onDelegateResponseMessage(ctx: ChannelHandlerContext, message: DelegateResponseMessage)

    fun onHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage)

    fun onMainBlockMessage(block: MainBlockMessage)

    fun onGenesisBlockMessage(block: GenesisBlockMessage)

}
