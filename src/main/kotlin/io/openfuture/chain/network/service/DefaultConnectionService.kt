package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DefaultConnectionService(
    private val bootstrap: Bootstrap,
    private val nodeProperties: NodeProperties,
    private val channelHolder: ChannelsHolder,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : ConnectionService {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultConnectionService::class.java)
    }


    override fun connect(networkAddress: NetworkAddress, close: Boolean) {
        bootstrap.connect(networkAddress.host, networkAddress.port).addListener { future ->
            val channel = (future as ChannelFuture).channel()
            if (future.isSuccess) {
                if (close) {
                    channel.close()
                    return@addListener
                }
            } else {
                log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")

                explorerAddressesHolder.getNodesInfo().firstOrNull { it.address == networkAddress }?.let {
                    explorerAddressesHolder.removeNodeInfo(it)
                }
            }
        }
    }

    @Scheduled(fixedRateString = "\${node.check-connection-period}")
    fun findNewPeers() {
        if (explorerAddressesHolder.getNodesInfo().isEmpty()) {
            connect(nodeProperties.getRootAddresses().shuffled().first())
            return
        }

        val neededPeers = nodeProperties.peersNumber!! - channelHolder.size()
        if (neededPeers > 0) {
            val uids = explorerAddressesHolder.getNodesInfo().map { it.uid }
                .minus(channelHolder.getNodesInfo().map { it.uid })

            if (uids.isEmpty()) {
                connect(nodeProperties.getRootAddresses().shuffled().first())
                return
            }

            uids.shuffled().take(neededPeers).forEach {
                connect(explorerAddressesHolder.getNodesInfo()
                    .first { nodeInfo -> nodeInfo.uid == it }.address)
            }
        }
    }

    @Scheduled(fixedRateString = "\${node.check-network}")
    fun checkNodes() {
        if (channelHolder.size() >= nodeProperties.peersNumber!!) {
            val uids = explorerAddressesHolder.getNodesInfo().map { it.uid }
                .minus(channelHolder.getNodesInfo().map { it.uid })

            if (nodeProperties.peersNumber!! > uids.size) {
                uids.forEach {
                    connect(explorerAddressesHolder.getNodesInfo()
                        .first { nodeInfo -> nodeInfo.uid == it }.address, true)
                }
            } else {
                uids.shuffled().take(nodeProperties.peersNumber!!).forEach {
                    connect(explorerAddressesHolder.getNodesInfo()
                        .first { nodeInfo -> nodeInfo.uid == it }.address, true)
                }
            }
        }
    }

}