package io.openfuture.chain.network.service

import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.entity.NodeInfo
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

    override fun isChannelsEmpty(): Boolean = channelsHolder.isEmpty()

    override fun getConnectionSize(): Int = channelsHolder.size()

    override fun sendRandom(message: Serializable) {
        channelsHolder.sendRandom(message)
    }

    override fun sendToAddress(message: Serializable, nodeInfo: NodeInfo) {
        if (!channelsHolder.send(message, nodeInfo)) {
            connectionService.connect(nodeInfo.address)
            channelsHolder.send(message, nodeInfo)
        }
    }

    override fun getNetworkSize(): Int = explorerAddressesHolder.getNodesInfo(false).size

}
