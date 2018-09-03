package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.exception.NotFoundRootNodeException
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.RequestPeersMessage
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DefaultConnectionService(
    private val config: NodeConfigurator,
    private val nodeKeyHolder: NodeKeyHolder,
    private val bootstrap: Bootstrap,
    private val nodeProperties: NodeProperties,
    private val channelHolder: ChannelsHolder
) : ConnectionService {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultConnectionService::class.java)
    }


    override fun connect(networkAddress: NetworkAddress) {
        bootstrap.connect(networkAddress.host, networkAddress.port).addListener { future ->
            val channel = (future as ChannelFuture).channel()
            if (future.isSuccess) {
                channel.writeAndFlush(GreetingMessage(config.getConfig().externalPort, nodeKeyHolder.getUid()))
                channelHolder.addPeer(channel, networkAddress)
            } else {
                log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")
                channel.close()
            }
        }
    }

    @Scheduled(fixedRateString = "\${node.check-connection-period}")
    fun findNewPeers() {
        if (channelHolder.getPeersAddresses().size < nodeProperties.peersNumber!!) {
            channelHolder.getAllChannels().shuffled().firstOrNull()?.let {
                it.writeAndFlush(RequestPeersMessage())
                return
            }

            connect(getRootNetworkAddress())
        }
    }

    private fun getRootNetworkAddress(): NetworkAddress = nodeProperties.getRootAddresses()
        .shuffled()
        .firstOrNull() ?: throw NotFoundRootNodeException("There are no available network addresses")

}