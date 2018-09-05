package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.lang.Math.max
import java.lang.Math.min

@Service
class DefaultConnectionService(
    private val config: NodeConfigurator,
    private val nodeKeyHolder: NodeKeyHolder,
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
                channel.writeAndFlush(GreetingMessage(config.getConfig().externalPort, nodeKeyHolder.getUid()))
                channelHolder.addChannel(channel, networkAddress)
            } else {
                log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")
                channel.close()
                explorerAddressesHolder.removeAddress(networkAddress)
            }
        }
    }

    @Scheduled(fixedRateString = "\${node.check-connection-period}")
    fun findNewPeers() {
        if (explorerAddressesHolder.getAddresses().isEmpty()) {
            explorerAddressesHolder.addAddresses(nodeProperties.getRootAddresses())
            connect(nodeProperties.getRootAddresses().shuffled().first())
            return
        }

        val neededPeers = max(nodeProperties.peersNumber!! - channelHolder.getChannels().size, 0)
        if (neededPeers > 0) {
            val addresses = explorerAddressesHolder.getAddresses().toMutableSet()
                .minus(channelHolder.getAddresses())

            if (addresses.isEmpty()) {
                return
            }

            for (i in 1..neededPeers) {
                connect(addresses.shuffled().first())
            }
        }
    }

    @Scheduled(fixedRateString = "\${node.check-network}")
    fun checkNodes() {
        if (channelHolder.getChannels().size >= nodeProperties.peersNumber!!) {
            val addresses = explorerAddressesHolder.getAddresses().toMutableSet()
                .minus(channelHolder.getAddresses())

            if (min(nodeProperties.peersNumber!!, addresses.size) == addresses.size) {
                addresses.forEach { connect(it, true) }
            } else {
                for (i in 1..nodeProperties.peersNumber!!) {
                    connect(addresses.shuffled().first(), true)
                }
            }
        }
    }

}