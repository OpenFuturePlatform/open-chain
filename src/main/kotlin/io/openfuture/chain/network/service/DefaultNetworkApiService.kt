package io.openfuture.chain.network.service

import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.base.Message
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class DefaultNetworkApiService(
    private val channelsHolder: ChannelsHolder,
    private val connectionService: ConnectionService,
    private val addressesHolder: AddressesHolder
) : NetworkApiService {

    override fun broadcast(message: Message) {
        channelsHolder.broadcast(message)
    }

    override fun isChannelsEmpty(): Boolean = channelsHolder.isEmpty()

    override fun getConnectionSize(): Int = channelsHolder.size()

    override fun sendToAddress(message: Message, nodeInfo: NodeInfo) {
        if (!channelsHolder.send(message, nodeInfo)) {
            connectionService.connect(nodeInfo.address, Consumer {
                it.attr(ChannelsHolder.NODE_INFO_KEY).set(nodeInfo)
                it.writeAndFlush(message)
            })
        }
    }

    override fun getNetworkSize(): Int = addressesHolder.getNodeInfos().size + 1

    override fun poll(message: Message, pollSize: Int) {
        addressesHolder.getRandomList(pollSize).forEach { sendToAddress(message, it) }
    }

}
