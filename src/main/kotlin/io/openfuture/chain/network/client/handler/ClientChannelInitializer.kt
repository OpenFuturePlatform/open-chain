package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelPipeline
import io.openfuture.chain.network.base.PacketDecoder
import io.openfuture.chain.network.base.PacketEncoder
import io.openfuture.chain.network.base.handler.BaseChannelInitializer
import io.openfuture.chain.network.base.handler.CommonHandler
import org.springframework.stereotype.Component

@Component
class ClientChannelInitializer(
        decoder: PacketDecoder,
        encoder: PacketEncoder,
        connectionHandler: ConnectionClientHandler,
        commonHandlers: Array<CommonHandler<*>>,
        private val clientHandlers: Array<ClientHandler<*>>
) : BaseChannelInitializer(decoder, encoder, connectionHandler, commonHandlers) {

    override fun initChannel(pipeline: ChannelPipeline) {
        pipeline.addLast(*clientHandlers)
    }

}