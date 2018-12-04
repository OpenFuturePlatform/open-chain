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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

@Service
class DefaultConnectionService(
    private val clock: Clock,
    @Lazy private val bootstrap: Bootstrap,
    private val nodeProperties: NodeProperties,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder,
    private val nodeKeyHolder: NodeKeyHolder
) : ConnectionService, ApplicationListener<ServerReadyEvent> {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultConnectionService::class.java)
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var connectionTask: Future<*>? = null

    override fun onApplicationEvent(event: ServerReadyEvent) {
        findNewPeer()
    }

    override fun connect(networkAddress: NetworkAddress, onConnect: Consumer<Channel>?): Boolean {
        var result = true
        try {
            bootstrap.connect(networkAddress.host, networkAddress.port).addListener { future ->
                if (future.isSuccess) {
                    onConnect?.let {
                        val channel = (future as ChannelFuture).channel()
                        it.accept(channel)
                    }
                } else {
                    log.warn("Can not connect to ${networkAddress.host}:${networkAddress.port}")
                    addressesHolder.removeNodeInfo(networkAddress)
                    result = false
                }
            }.sync()
        } catch (ex: Exception) {
            result = false
        }
        return result
    }

    override fun sendTimeSyncRequest() {
        val addresses = addressesHolder
            .getRandomList()
            .map { it.address }
        var numberOfConnections = nodeProperties.getRootAddresses().size
        val it = addresses.iterator()
        while (it.hasNext() && numberOfConnections != 0) {
            val address = it.next()
            val connected = connect(address, Consumer {
                val message = RequestTimeMessage(clock.currentTimeMillis())
                it.writeAndFlush(message)
            })
            if (connected) {
                numberOfConnections--
            }
        }
    }

    override fun findNewPeer() {
        if (nodeProperties.peersNumber!! > channelHolder.size() && (null == connectionTask || connectionTask!!.isDone)) {
            connectionTask = executor.submit { findNewPeer0() }
        }
    }

    private fun findNewPeer0() {
        if (addressesHolder.size() < nodeProperties.rootNodes.size) {
            while (!findBootNode() && nodeProperties.rootNodes.size > channelHolder.size()) {
                log.warn("Unable to find boot peer. Retry...")
                TimeUnit.SECONDS.sleep(3)
            }
        } else {
            findRegularPeer()
        }
    }

    private fun findBootNode(): Boolean {
        log.info("Searching boot peers ${channelHolder.getNodesInfo().joinToString { it.address.port.toString() }} | ${channelHolder.size()}")
        val connectedPeers = channelHolder.getNodesInfo().map { it.address }
        for (bootAddress in nodeProperties.getRootAddresses().minus(connectedPeers).shuffled()) {
            val connected = connect(bootAddress, Consumer {
                greet(it, bootAddress)
            })
            if (connected) return true
        }
        return false
    }

    private fun findRegularPeer(): Boolean {
        log.info("Looking for regular peer ${channelHolder.getNodesInfo().joinToString { it.address.port.toString() }} | ${channelHolder.size()}")
        val knownPeers = channelHolder.getNodesInfo()
        for (peer in addressesHolder.getRandomList(connectedPeers = knownPeers)) {
            if (!addressesHolder.isRejected(peer.address)) {
                val connected = connect(peer.address, Consumer {
                    greet(it, peer.address)
                })
                if (connected) return true
            }
        }
        return false
    }

    private fun greet(channel: Channel, address: NetworkAddress): Boolean {
        val message = GreetingMessage(nodeProperties.port!!, nodeKeyHolder.getUid())
        channel.writeAndFlush(message).addListener {
            if (it.isSuccess) {
                log.info("Sent greeting to ${address.host}:${address.port} success")
            } else {
                log.info("Sent greeting to ${address.host}:${address.port} failed")
            }
        }
        return true
    }

    @Scheduled(fixedRateString = "\${node.peer-unavailability-period}")
    fun maintain() {
        addressesHolder.cancelRejectedStatus()
        findNewPeer()
    }

}