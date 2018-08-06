package io.openfuture.chain.network.handler.server

import io.netty.channel.ChannelHandler
import io.openfuture.chain.network.handler.base.BaseConnectionHandler
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler : BaseConnectionHandler()