package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandler
import io.openfuture.chain.core.sync.SyncBlockHandler
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.InnerNetworkService
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    networkService: InnerNetworkService,
    coreService: CoreMessageService,
    consensusService: ConsensusMessageService,
    lock: ReentrantReadWriteLock,
    syncBlockHandler: SyncBlockHandler
) : BaseConnectionHandler(lock, coreService, syncBlockHandler, networkService, consensusService)