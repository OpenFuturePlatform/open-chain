package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelPipeline
import io.openfuture.chain.network.base.handler.BaseChannelInitializer
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ServerChannelInitializer(
    context: ApplicationContext
) : BaseChannelInitializer(context, ConnectionServerHandler::class.java) {

    override fun initChannel(pipeline: ChannelPipeline) {
        pipeline.addLast(context.getBean(TimeSyncServerHandler::class.java))
        pipeline.addLast(context.getBean(HeartBeatServerHandler::class.java))
    }

}