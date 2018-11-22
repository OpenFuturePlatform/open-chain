package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.RequestTimeMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.server.ServerReadyEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

@Service
class DefaultConnectionService(
    private val clock: Clock,
    @Lazy private val bootstrap: Bootstrap,
    private val nodeProperties: NodeProperties,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder,
    private val config: NodeConfigurator,
    private val nodeKeyHolder: NodeKeyHolder
) : ConnectionService, ApplicationListener<ServerReadyEvent> {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultConnectionService::class.java)
    }


    override fun onApplicationEvent(event: ServerReadyEvent) {
        findNewPeer()
    }

    override fun connect(networkAddress: NetworkAddress, onConnect: Consumer<Channel>?): ChannelFuture {
        return bootstrap.connect(networkAddress.host, networkAddress.port).addListener { future ->
            if (future.isSuccess) {
                onConnect?.let {
                    val channel = (future as ChannelFuture).channel()
                    it.accept(channel)
                }
            } else {
                log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")
                addressesHolder.removeNodeInfo(networkAddress)
            }
        }.sync()
    }

    override fun sendTimeSyncRequest() {
        val addresses = addressesHolder
            .getRandomList(nodeProperties.getRootAddresses().size)
            .map { it.address }
        addresses.forEach { networkAddress ->
            connect(networkAddress, Consumer {
                val message = RequestTimeMessage(clock.currentTimeMillis())
                it.writeAndFlush(message)
            })
        }
    }

    @Synchronized
    override fun findNewPeer() {
        if (nodeProperties.peersNumber!! <= channelHolder.size()) {
            return
        }
        if (addressesHolder.size() <= nodeProperties.peersNumber!!) {
            while (!findBootNode()) {
                log.warn("Unable to find boot peer. Retry...")
                TimeUnit.SECONDS.sleep(3)
            }
            return
        }
        findRegularPeer()
    }

    private fun findBootNode(): Boolean {
        val connectedPeers = channelHolder.getNodesInfo().map { it.address }
        for (bootAddress in nodeProperties.getRootAddresses().minus(connectedPeers)) {
            if (addressesHolder.isRejected(bootAddress)) {
                continue
            }
            val channelFuture = connect(bootAddress, Consumer {
                greet(it, bootAddress)
            })
            if (channelFuture.isSuccess) return true
        }
        return false
    }

    private fun findRegularPeer(): Boolean {
        var connected = false
        val knownPeers = channelHolder.getNodesInfo().toMutableList()
        for (i in 0 until addressesHolder.size()) {
            val peer = addressesHolder.getRandom(channelHolder.getNodesInfo())
            connect(peer.address, Consumer {
                connected = greet(it, peer.address)
            })
            if (connected) return true
            knownPeers.add(peer)
        }
        return false
    }

    private fun greet(channel: Channel, address: NetworkAddress): Boolean {
        log.info("Connected to ${address.host}:${address.port}")
        val message = GreetingMessage(config.getConfig().externalPort, nodeKeyHolder.getUid())
        channel.writeAndFlush(message)
        return true
    }

    @Scheduled(fixedRateString = "\${node.peer-unavailability-period}")
    fun maintain() {
        addressesHolder.cancelRejectedStatus()
    }

}