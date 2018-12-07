package io.openfuture.chain.network.handler.network.initializer

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import io.openfuture.chain.network.handler.consensus.BlockApprovalHandler
import io.openfuture.chain.network.handler.consensus.PendingBlockNetworkHandler
import io.openfuture.chain.network.handler.core.DelegateTransactionHandler
import io.openfuture.chain.network.handler.core.TransferTransactionHandler
import io.openfuture.chain.network.handler.core.VoteTransactionHandler
import io.openfuture.chain.network.handler.network.*
import io.openfuture.chain.network.handler.network.codec.MessageCodec
import io.openfuture.chain.network.handler.network.server.GreetingHandler
import io.openfuture.chain.network.handler.network.server.RequestTimeHandler
import io.openfuture.chain.network.handler.sync.*
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ServerChannelInitializer(
    private val nodeProperties: NodeProperties,
    private val applicationContext: ApplicationContext,
    private val cacheHandler: CacheHandler,
    private val requestCountHandler: RequestCountHandler
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val readIdleTime = (nodeProperties.heartBeatInterval!! + nodeProperties.connectionTimeout!!).toLong()
        val writeIdleTime = nodeProperties.heartBeatInterval!!.toLong()

        val pipeline = ch.pipeline()

        pipeline.addLast(
            IdleStateHandler(readIdleTime, writeIdleTime, 0, TimeUnit.MILLISECONDS),
            cacheHandler,
            requestCountHandler,
            applicationContext.getBean(MessageCodec::class.java),
            applicationContext.getBean(ConnectionHandler::class.java),
            applicationContext.getBean(HeartBeatHandler::class.java),
            applicationContext.getBean(GreetingHandler::class.java),
            applicationContext.getBean(RequestTimeHandler::class.java),
            applicationContext.getBean(NewClientHandler::class.java),
            applicationContext.getBean(NetworkStatusHandler::class.java),
            //        sync
            applicationContext.getBean(SyncRequestHandler::class.java),
            applicationContext.getBean(SyncResponseHandler::class.java),
            applicationContext.getBean(SyncBlockRequestHandler::class.java),
            applicationContext.getBean(MainBlockHandler::class.java),
            applicationContext.getBean(GenesisBlockHandler::class.java),
            applicationContext.getBean(SyncStatusHandler::class.java),
            //        core
            applicationContext.getBean(TransferTransactionHandler::class.java),
            applicationContext.getBean(DelegateTransactionHandler::class.java),
            applicationContext.getBean(VoteTransactionHandler::class.java),
            //        consensus
            applicationContext.getBean(PendingBlockNetworkHandler::class.java),
            applicationContext.getBean(BlockApprovalHandler::class.java)
        )
    }

}