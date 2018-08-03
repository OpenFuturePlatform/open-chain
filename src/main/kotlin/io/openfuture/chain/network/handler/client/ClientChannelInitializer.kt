package io.openfuture.chain.network.handler.client

import io.openfuture.chain.network.handler.base.BaseChannelInitializer
import org.springframework.stereotype.Component

@Component
class ClientChannelInitializer(
    connectionHandler: ConnectionClientHandler
) : BaseChannelInitializer(connectionHandler)