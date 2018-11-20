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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.annotation.PostConstruct

@Service
class DefaultConnectionService(
    private val clock: Clock,
    private val bootstrap: Bootstrap,
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

    override fun connect(networkAddress: NetworkAddress, onConnect: Consumer<Channel>?) {
        bootstrap.connect(networkAddress.host, networkAddress.port).addListener { future ->
            if (future.isSuccess) {
                onConnect?.let {
                    val channel = (future as ChannelFuture).channel()
                    it.accept(channel)
                }
            } else {
                log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")
                addressesHolder.removeNodeInfo(networkAddress)
            }
            return@addListener
        }.await(3, TimeUnit.SECONDS)
    }

    override fun sendTimeSyncRequest(addresses: Set<NetworkAddress>) {
        addresses.forEach { networkAddress ->
            connect(networkAddress, Consumer {
                val message = RequestTimeMessage(clock.currentTimeMillis())
                it.writeAndFlush(message)
            })
        }
    }

    override fun findNewPeer() {
        if (nodeProperties.peersNumber!! <= channelHolder.size()) {
            return
        }
        if (addressesHolder.size() <= nodeProperties.peersNumber!!) {
            while(!findBootNode() && nodeProperties.peersNumber!! <= channelHolder.size()) {
                log.info("Unable to find boot peer. Waiting...")
                Thread.sleep(3000)
            }
            return
        }
        findRegularPeer()
    }

    private fun findRegularPeer() {
        var connected = false
        val knownPeers = channelHolder.getNodesInfo().toMutableList()
        for (i in 0 until addressesHolder.size()) {
            val peer = addressesHolder.getRandom(connectedPeers = channelHolder.getNodesInfo())
            connect(peer.address, Consumer {
                val message = GreetingMessage(config.getConfig().externalPort, nodeKeyHolder.getUid())
                it.writeAndFlush(message)
                log.info("Connected to ${peer.address.host}:${peer.address.port}")
                connected = true
            })
            if (connected) {
                break
            }
            knownPeers.add(peer)
        }
    }

    private fun findBootNode(): Boolean {
        var connected = false
        val connectedPeers = channelHolder.getNodesInfo().map { it.address }
        for (bootAddress in nodeProperties.getRootAddresses().minus(connectedPeers)) {
            connect(bootAddress, Consumer {
                val message = GreetingMessage(config.getConfig().externalPort, nodeKeyHolder.getUid())
                it.writeAndFlush(message)
                log.info("Connected to ${bootAddress.host}:${bootAddress.port}")
                connected = true
            })
            if (connected) return true
        }
        return false
    }

    @Scheduled(fixedRateString = "\${node.peer-unavailability-period}")
    fun maintain() {
        addressesHolder.cancelRejectedStatus()
    }

}