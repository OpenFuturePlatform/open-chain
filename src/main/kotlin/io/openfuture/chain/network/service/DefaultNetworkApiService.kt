package io.openfuture.chain.network.service

import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.network.AddressMessage
import org.springframework.stereotype.Service

@Service
class DefaultNetworkApiService(
    private val networkInnerService: DefaultNetworkInnerService
) : NetworkApiService {

    override fun broadcast(message: BaseMessage) {
        networkInnerService.getChannels().forEach {
            it.writeAndFlush(message)
        }
    }

    override fun send(message: BaseMessage) {
        networkInnerService.getChannels().shuffled().firstOrNull()?.writeAndFlush(message)
    }

    override fun sendToAddress(message: BaseMessage, addressMessage: AddressMessage) {
        networkInnerService.sendToAddress(message, addressMessage)
    }

    override fun sendToRootNode(message: BaseMessage) {
        networkInnerService.sendToRootNode(message)
    }

    override fun getNetworkSize(): Int = networkInnerService.getNetworkSize()

    override fun isChannelsEmpty(): Boolean = networkInnerService.getChannels().isEmpty()

}
