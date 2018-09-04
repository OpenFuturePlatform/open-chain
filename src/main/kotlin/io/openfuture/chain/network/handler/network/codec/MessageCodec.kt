package io.openfuture.chain.network.handler.network.codec

import io.netty.channel.CombinedChannelDuplexHandler
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class MessageCodec(
    decoder: MessageDecoder,
    encoder: MessageEncoder
) : CombinedChannelDuplexHandler<MessageDecoder, MessageEncoder>(decoder, encoder)