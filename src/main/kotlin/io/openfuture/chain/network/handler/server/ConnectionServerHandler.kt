package io.openfuture.chain.network.handler.server

import io.netty.channel.ChannelHandler
import io.openfuture.chain.network.handler.base.BaseConnectionHandler
import io.openfuture.chain.network.service.DefaultApplicationMessageService
import io.openfuture.chain.network.service.NetworkMessageService
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    networkService: NetworkMessageService,
    applicationService: DefaultApplicationMessageService
) : BaseConnectionHandler(networkService, applicationService)