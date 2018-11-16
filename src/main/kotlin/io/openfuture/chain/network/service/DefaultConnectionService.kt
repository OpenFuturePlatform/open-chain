package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.RequestTimeMessage
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class DefaultConnectionService(
    private val clock: Clock,
    private val bootstrap: Bootstrap,
    private val nodeProperties: NodeProperties,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder
) : ConnectionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultConnectionService::class.java)
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
        }
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
        var connected = false
        val knownPeers = mutableListOf(*channelHolder.getNodesInfo().toTypedArray())
        for (i in 0 until addressesHolder.size()) {
            val peer = addressesHolder.getRandom(connectedPeers = channelHolder.getNodesInfo())
            connect(peer.address, Consumer { connected = true })
            if (connected) {
                break
            }
            knownPeers.add(peer)
        }
    }

    @Scheduled(fixedRateString = "\${node.peer-unavailability-period}")
    fun maintain() {
        addressesHolder.cancelRejectedStatus()
    }

}