package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandler
import io.openfuture.chain.network.sync.SyncBlockHandler
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.NetworkInnerService
import io.openfuture.chain.network.sync.SyncManager
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    networkService: NetworkInnerService,
    coreService: CoreMessageService,
    consensusService: ConsensusMessageService,
    syncManager: SyncManager,
    syncBlockHandler: SyncBlockHandler
) : BaseConnectionHandler(coreService, syncManager, syncBlockHandler, networkService, consensusService)