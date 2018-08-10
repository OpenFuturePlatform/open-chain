package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandler
import io.openfuture.chain.network.sync.SyncBlockHandler
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.NetworkInnerService
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    networkService: NetworkInnerService,
    coreService: CoreMessageService,
    consensusService: ConsensusMessageService,
    syncBlockHandler: SyncBlockHandler
) : BaseConnectionHandler(coreService, syncBlockHandler, networkService, consensusService)