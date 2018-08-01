package io.openfuture.chain.network.handler.client

import io.openfuture.chain.network.handler.base.BaseChannelInitializer
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ClientChannelInitializer(
    connectionHandler: ConnectionClientHandler,
    context: ApplicationContext
) : BaseChannelInitializer(connectionHandler, context)