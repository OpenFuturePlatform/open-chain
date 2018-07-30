package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelPipeline
import io.openfuture.chain.network.base.handler.BaseChannelInitializer
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ClientChannelInitializer(
    context: ApplicationContext
) : BaseChannelInitializer(context, ConnectionClientHandler::class.java) {

    override fun initChannel(pipeline: ChannelPipeline) {
        pipeline.addLast(context.getBean(TimeSyncClientHandler::class.java))
        pipeline.addLast(context.getBean(MainBlockClientHandler::class.java))
        pipeline.addLast(context.getBean(GenesisBlockClientHandler::class.java))
        pipeline.addLast(context.getBean(HeartBeatClientHandler::class.java))
    }

}