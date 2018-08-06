package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandler
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.InnerNetworkService
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    networkService: InnerNetworkService,
    coreService: CoreMessageService,
    consensusService: ConsensusMessageService
) : BaseConnectionHandler(networkService, coreService, consensusService)