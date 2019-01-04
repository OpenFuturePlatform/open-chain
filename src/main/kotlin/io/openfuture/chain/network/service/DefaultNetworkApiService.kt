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

    override fun sendToAddress(message: Serializable, nodeInfo: NodeInfo): Boolean {
        var isSent = channelsHolder.send(message, nodeInfo)
        if (!isSent) {
            isSent = connectionService.connect(nodeInfo.address, Consumer {
                it.attr(ChannelsHolder.NODE_INFO_KEY).set(nodeInfo)
                it.writeAndFlush(message)
            })
        }
        return isSent

    }

    override fun getNetworkSize(): Int = addressesHolder.getNodeInfos().size + 1

    override fun poll(message: Serializable, pollSize: Int) {
        addressesHolder.getRandomList(pollSize).forEach { sendToAddress(message, it) }
    }

}
