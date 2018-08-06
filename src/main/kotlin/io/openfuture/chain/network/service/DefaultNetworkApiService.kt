package io.openfuture.chain.network.service

import io.openfuture.chain.network.message.base.BaseMessage
import org.springframework.stereotype.Service

@Service
class DefaultNetworkApiService(
    private val networkInnerService: DefaultNetworkInnerService
) : NetworkApiService{

    override fun broadcast(message: BaseMessage) {
        networkInnerService.getChannels().forEach {
            it.writeAndFlush(message)
        }
    }

    override fun send(message: BaseMessage) {
        networkInnerService.getChannels().shuffled().firstOrNull()?.writeAndFlush(message)
    }

}
