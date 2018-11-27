package io.openfuture.chain.network.service

import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefaultNetworkApiService(
    private val channelsHolder: ChannelsHolder,
    private val connectionService: ConnectionService,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : NetworkApiService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultNetworkApiService::class.java)
    }


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
            log.debug("CAN NOT SEND to ${nodeInfo.address.port}")
            connectionService.connect(nodeInfo.address)
            channelsHolder.send(message, nodeInfo)
        }
    }

    override fun getNetworkSize(): Int = explorerAddressesHolder.getNodesInfo(false).size

    override fun poll(message: Serializable, pollSize: Int) {
        explorerAddressesHolder.getRandomList(pollSize).forEach { sendToAddress(message, it) }
    }

}
