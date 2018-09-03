package io.openfuture.chain.network.service

import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.stereotype.Service

@Service
class DefaultNetworkApiService(
    private val channelsHolder: ChannelsHolder,
    private val connectionService: ConnectionService,
    private val networkInnerService: NetworkInnerService
) : NetworkApiService {

    override fun broadcast(message: Serializable) {
        channelsHolder.broadcast(message)
    }

    override fun isChannelsEmpty(): Boolean = channelsHolder.isChannelsEmpty()

    override fun getConnectionSize(): Int = channelsHolder.getChannelsCount()

    override fun send(message: Serializable) {
        channelsHolder.send(message)
    }

    override fun sendToAddress(message: Serializable, address: NetworkAddress) {
        if (!channelsHolder.send(message, address)) {
            connectionService.connect(address)
        }
    }

    override fun getNetworkSize(): Int = networkInnerService.getNetworkSize()

}
