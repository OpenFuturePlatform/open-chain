package io.openfuture.chain.network.handler.network.initializer

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.openfuture.chain.network.handler.consensus.BlockApprovalHandler
import io.openfuture.chain.network.handler.consensus.PendingBlockNetworkHandler
import io.openfuture.chain.network.handler.core.DelegateTransactionHandler
import io.openfuture.chain.network.handler.core.TransferTransactionHandler
import io.openfuture.chain.network.handler.core.VoteTransactionHandler
import io.openfuture.chain.network.handler.network.HeartBeatHandler
import io.openfuture.chain.network.handler.network.RequestPeersHandler
import io.openfuture.chain.network.handler.network.ResponsePeersHandler
import io.openfuture.chain.network.handler.network.codec.MessageCodec
import io.openfuture.chain.network.handler.network.server.GreetingHandler
import io.openfuture.chain.network.handler.network.server.RequestTimeHandler
import io.openfuture.chain.network.handler.sync.*
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ServerChannelInitializer(
    private val applicationContext: ApplicationContext
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()

        pipeline.addLast(applicationContext.getBean(MessageCodec::class.java))
        pipeline.addLast(applicationContext.getBean(HeartBeatHandler::class.java))
        pipeline.addLast(applicationContext.getBean(RequestTimeHandler::class.java))
        pipeline.addLast(applicationContext.getBean(GreetingHandler::class.java))
        pipeline.addLast(applicationContext.getBean(RequestPeersHandler::class.java))
        pipeline.addLast(applicationContext.getBean(ResponsePeersHandler::class.java))
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