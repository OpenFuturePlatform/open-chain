package io.openfuture.chain.network.handler.server

import io.openfuture.chain.network.handler.base.BaseChannelInitializer
import org.springframework.stereotype.Component

@Component
class ServerChannelInitializer(
    connectionHandler: ConnectionServerHandler
) : BaseChannelInitializer(connectionHandler)