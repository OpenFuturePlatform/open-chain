package io.openfuture.chain.network.service

import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class DefaultNetworkApiService(
    private val channelsHolder: ChannelsHolder,
    private val connectionService: ConnectionService,
    private val addressesHolder: AddressesHolder
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
            connectionService.connect(nodeInfo.address, Consumer {
                it.writeAndFlush(message)
            })
        }
    }

    override fun getNetworkSize(): Int = addressesHolder.getNodesInfo().size + 1

}
