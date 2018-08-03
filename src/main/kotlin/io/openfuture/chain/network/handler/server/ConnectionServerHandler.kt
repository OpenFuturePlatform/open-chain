package io.openfuture.chain.network.handler.server

import io.netty.channel.ChannelHandler
import io.openfuture.chain.network.handler.base.BaseConnectionHandler
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.DefaultCoreMessageService
import io.openfuture.chain.network.service.NetworkMessageService
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    networkService: NetworkMessageService,
    coreService: DefaultCoreMessageService,
    consensusService: ConsensusMessageService
) : BaseConnectionHandler(networkService, coreService, consensusService)