package io.openfuture.chain.network.service

import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.stereotype.Service

@Service
class DefaultNetworkApiService(
    private val channelsHolder: ChannelsHolder,
    private val connectionService: ConnectionService,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : NetworkApiService {

    override fun broadcast(message: Serializable) {
        channelsHolder.broadcast(message)
    }

    override fun isChannelsEmpty(): Boolean = channelsHolder.getChannels().isEmpty()

    override fun getConnectionSize(): Int = channelsHolder.getChannels().size

    override fun sendRandom(message: Serializable) {
        channelsHolder.sendRandom(message)
    }

    override fun sendToAddress(message: Serializable, address: NetworkAddress) {
        if (!channelsHolder.send(message, address)) {
            connectionService.connect(address)
        }
    }

    override fun getNetworkSize(): Int = explorerAddressesHolder.getAddresses().size

}
