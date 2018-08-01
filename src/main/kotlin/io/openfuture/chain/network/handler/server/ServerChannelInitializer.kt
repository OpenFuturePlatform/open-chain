package io.openfuture.chain.network.handler.server

import io.openfuture.chain.network.handler.base.BaseChannelInitializer
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ServerChannelInitializer(
    connectionHandler: ConnectionServerHandler,
    context: ApplicationContext
) : BaseChannelInitializer(connectionHandler, context)