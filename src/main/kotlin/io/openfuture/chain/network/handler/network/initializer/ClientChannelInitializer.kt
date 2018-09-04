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

        pipeline.addLast(applicationContext.getBean(MessageCodec::class.java))
        pipeline.addLast(applicationContext.getBean(ConnectionHandler::class.java))
        pipeline.addLast(IdleStateHandler(readIdleTime, writeIdleTime, 0, TimeUnit.MILLISECONDS))
        pipeline.addLast(applicationContext.getBean(HeartBeatHandler::class.java))
        pipeline.addLast(applicationContext.getBean(ResponseTimeHandler::class.java))
        pipeline.addLast(applicationContext.getBean(GreetingResponseHandler::class.java))
        //        core
        pipeline.addLast(applicationContext.getBean(TransferTransactionHandler::class.java))
        pipeline.addLast(applicationContext.getBean(DelegateTransactionHandler::class.java))
        pipeline.addLast(applicationContext.getBean(VoteTransactionHandler::class.java))
        //        consensus
        pipeline.addLast(applicationContext.getBean(PendingBlockNetworkHandler::class.java))
        pipeline.addLast(applicationContext.getBean(BlockApprovalHandler::class.java))
        //        sync
        pipeline.addLast(applicationContext.getBean(DelegateRequestHandler::class.java))
        pipeline.addLast(applicationContext.getBean(DelegateResponseHandler::class.java))
        pipeline.addLast(applicationContext.getBean(HashBlockRequestHandler::class.java))
        pipeline.addLast(applicationContext.getBean(HashBlockResponseHandler::class.java))
        pipeline.addLast(applicationContext.getBean(SyncBlockRequestHandler::class.java))
        pipeline.addLast(applicationContext.getBean(MainBlockHandler::class.java))
        pipeline.addLast(applicationContext.getBean(GenesisBlockHandler::class.java))
    }

}