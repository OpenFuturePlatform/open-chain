package io.openfuture.chain.network.server.handler

import io.openfuture.chain.network.base.handler.BaseConnectionHandler
import io.openfuture.chain.network.domain.PacketType.*
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class ConnectionServerHandler : BaseConnectionHandler(
    setOf(ADDRESSES, FIND_ADDRESSES, GREETING, HEART_BEAT, ASK_TIME, SYNC_BLOCKS_REQUEST, BLOCK_APPROVAL_MESSAGE))