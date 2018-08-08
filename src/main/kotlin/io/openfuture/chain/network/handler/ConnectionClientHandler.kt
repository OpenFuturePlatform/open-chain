package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.sync.SyncBlockHandler
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.InnerNetworkService
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
@ChannelHandler.Sharable
class ConnectionClientHandler(
    networkService: InnerNetworkService,
    coreService: CoreMessageService,
    consensusService: ConsensusMessageService,
    lock: ReentrantReadWriteLock,
    syncBlockHandler: SyncBlockHandler
) : BaseConnectionHandler(lock, coreService, syncBlockHandler, networkService, consensusService) {

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        networkService.onClientChannelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        networkService.onClientChannelInactive(ctx)
    }

}