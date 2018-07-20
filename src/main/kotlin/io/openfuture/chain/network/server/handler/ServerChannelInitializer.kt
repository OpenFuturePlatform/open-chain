package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelPipeline
import io.openfuture.chain.network.base.PacketDecoder
import io.openfuture.chain.network.base.PacketEncoder
import io.openfuture.chain.network.base.handler.BaseChannelInitializer
import io.openfuture.chain.network.base.handler.CommonHandler
import org.springframework.stereotype.Component

@Component
class ServerChannelInitializer(
        decoder: PacketDecoder,
        encoder: PacketEncoder,
        connectionHandler: ConnectionServerHandler,
        commonHandlers: Array<CommonHandler<*>>,
        private val serverHandlers: Array<ServerHandler<*>>
) : BaseChannelInitializer(decoder, encoder, connectionHandler, commonHandlers) {

    override fun initChannel(pipeline: ChannelPipeline) {
        pipeline.addLast(*serverHandlers)
    }

}