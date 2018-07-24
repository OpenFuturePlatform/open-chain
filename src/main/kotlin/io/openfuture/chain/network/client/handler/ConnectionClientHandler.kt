package io.openfuture.chain.network.client.handler

import io.openfuture.chain.network.base.handler.BaseConnectionHandler
import io.openfuture.chain.network.domain.PacketType.*
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class ConnectionClientHandler : BaseConnectionHandler(setOf(
        ADDRESSES,
        FIND_ADDRESSES,
        GREETING,
        HEART_BEAT,
        TIME
))