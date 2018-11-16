package io.openfuture.chain.network.handler.network.initializer

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.timeout.IdleStateHandler
import io.openfuture.chain.network.handler.consensus.BlockApprovalHandler
import io.openfuture.chain.network.handler.consensus.PendingBlockNetworkHandler
import io.openfuture.chain.network.handler.core.DelegateTransactionHandler
import io.openfuture.chain.network.handler.core.TransferTransactionHandler
import io.openfuture.chain.network.handler.core.VoteTransactionHandler
import io.openfuture.chain.network.handler.network.ConnectionHandler
import io.openfuture.chain.network.handler.network.HeartBeatHandler
import io.openfuture.chain.network.handler.network.NetworkStatusHandler
import io.openfuture.chain.network.handler.network.NewClientHandler
import io.openfuture.chain.network.handler.network.client.GreetingResponseHandler
import io.openfuture.chain.network.handler.network.client.ResponseTimeHandler
import io.openfuture.chain.network.handler.network.codec.MessageCodec
import io.openfuture.chain.network.handler.sync.*
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ClientChannelInitializer(
    private val nodeProperties: NodeProperties,
    private val applicationContext: ApplicationContext
) : ChannelInitializer<Channel>() {

    override fun initChannel(channel: Channel) {
        val readIdleTime = (nodeProperties.heartBeatInterval!! + nodeProperties.connectionTimeout!!).toLong()
        val writeIdleTime = nodeProperties.heartBeatInterval!!.toLong()

        val pipeline = channel.pipeline()

        pipeline.addLast(
            applicationContext.getBean(MessageCodec::class.java),
            applicationContext.getBean(ConnectionHandler::class.java),
            IdleStateHandler(readIdleTime, writeIdleTime, 0, TimeUnit.MILLISECONDS),
            applicationContext.getBean(HeartBeatHandler::class.java),
            applicationContext.getBean(GreetingResponseHandler::class.java),
            applicationContext.getBean(ResponseTimeHandler::class.java),
            applicationContext.getBean(NewClientHandler::class.java),
            applicationContext.getBean(NetworkStatusHandler::class.java),
            //        sync
            applicationContext.getBean(SyncClockStatusHandler::class.java),
            applicationContext.getBean(SyncRequestHandler::class.java),
            applicationContext.getBean(SyncResponseHandler::class.java),
            applicationContext.getBean(SyncBlockRequestHandler::class.java),
            applicationContext.getBean(MainBlockHandler::class.java),
            applicationContext.getBean(GenesisBlockHandler::class.java),
            applicationContext.getBean(SyncChainStatusHandler::class.java),
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