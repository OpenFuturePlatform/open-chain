package io.openfuture.chain.network.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.core.DelegateResponseMessage
import io.openfuture.chain.network.message.core.GenesisBlockMessage
import io.openfuture.chain.network.message.core.HashBlockResponseMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.network.AddressMessage

interface SyncBlockResponseHandler {

    fun synchronize()

    fun getLastResponseTime(): Long

    fun onDelegateResponseMessage(ctx: ChannelHandlerContext, message: DelegateResponseMessage)

    fun onHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage, addressMessage: AddressMessage)

    fun onMainBlockMessage(block: MainBlockMessage)

    fun onGenesisBlockMessage(block: GenesisBlockMessage)

}
