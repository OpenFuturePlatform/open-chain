package io.openfuture.chain.network.server.handler

import io.openfuture.chain.network.base.handler.BaseConnectionHandler
import io.openfuture.chain.network.domain.*
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class ConnectionServerHandler : BaseConnectionHandler(setOf(
        Addresses::class,
        FindAddresses::class,
        Greeting::class,
        HeartBeat::class,
        AskTime::class
))