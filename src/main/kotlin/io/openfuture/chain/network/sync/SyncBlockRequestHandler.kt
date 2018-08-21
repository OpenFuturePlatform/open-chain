package io.openfuture.chain.network.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.core.*

interface SyncBlockRequestHandler {

    fun onDelegateRequestMessage(ctx: ChannelHandlerContext, message: DelegateRequestMessage)

    fun onLastHashRequestMessage(ctx: ChannelHandlerContext, message: HashBlockRequestMessage)

    fun onSyncBlocRequestMessage(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage)

}
