package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.RequestTimeMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DefaultConnectionService(
    private val clock: Clock,
    private val bootstrap: Bootstrap,
    private val nodeProperties: NodeProperties,
    private val channelHolder: ChannelsHolder,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : ConnectionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultConnectionService::class.java)
    }


    override fun connect(networkAddress: NetworkAddress) {
        bootstrap.connect(networkAddress.host, networkAddress.port).addListener { future ->
            if (!future.isSuccess) {
                log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")
                explorerAddressesHolder.removeNodeInfo(networkAddress)
            }
            return@addListener
        }
    }

    override fun sendTimeSyncRequest(addresses: Set<NetworkAddress>) {
        addresses.forEach { address ->
            bootstrap.connect(address.host, address.port).addListener { future ->
                val channel = (future as ChannelFuture).channel()

                if (future.isSuccess) {
                    channel.writeAndFlush(RequestTimeMessage(clock.currentTimeMillis()))
                    log.error("Send Clock Request to ${address.port}")
                }
            }
        }
    }

    override fun poll(message: Serializable, pollSize: Int) {
        explorerAddressesHolder.getRandomList(pollSize).map { it.address }.forEach { address ->
            bootstrap.connect(address.host, address.port).addListener { future ->
                val channel = (future as ChannelFuture).channel()

                if (future.isSuccess) {
                    channel.writeAndFlush(message)
                    log.error("Send ${message::class.java.simpleName} to ${address.port}")
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
            log.warn("Need  $neededPeers peers, current peers count ${channelHolder.size()}")
            val uids = explorerAddressesHolder.getNodesInfo().asSequence().map { it.uid }
                .minus(channelHolder.getNodesInfo().map { it.uid }).toList()

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

}