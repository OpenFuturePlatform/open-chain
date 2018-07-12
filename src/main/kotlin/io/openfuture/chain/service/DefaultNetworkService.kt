package io.openfuture.chain.service

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.domain.Peer
import io.openfuture.chain.network.server.TcpServer
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocol
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Component
class DefaultNetworkService(
    private val clientBootstrap: Bootstrap,
    private val tcpServer: TcpServer,
    private val properties: NodeProperties
) : NetworkService, ApplicationListener<ApplicationReadyEvent> {

    private val connectedPeers : MutableMap<Channel, Peer> = ConcurrentHashMap()
    private val knownPeers : MutableSet<Peer> = ConcurrentHashMap.newKeySet()
    private val networkId = properties.host!! + properties.port

    companion object {
        private val log = LoggerFactory.getLogger(DefaultNetworkService::class.java)
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        Executors.newSingleThreadExecutor().execute(tcpServer)

        val shuffledNodes = properties.rootNodes.shuffled(SecureRandom())
        val address = shuffledNodes[0].split(":")
        clientBootstrap.connect(address[0], address[1].toInt()).addListener { future ->
            future as ChannelFuture
            if (future.isSuccess) {
                future.channel().writeAndFlush(createGetPeersMessage())
            } else {
                log.warn("Can not connect to ${address[0]}:${address[1]}")
            }
        }
    }

    @Scheduled(cron="*/30 * * * * *")
    override fun maintainConnectionNumber() {
        val connectionNeeded = properties.peersNumber!! - connectedPeers.size
        val peers = knownPeers.shuffled(SecureRandom())
        for (peer in peers) {
            if (peer.networkId != networkId && !isConnected(peer.networkId)) {
                clientBootstrap.connect(peer.host, peer.port).addListener {
                    future -> future as ChannelFuture
                        if (!future.isSuccess) {
                            log.warn("Can not connect to ${peer.host}:${peer.port}")
                        }
                }
                connectionNeeded.dec()
                if (connectionNeeded == 0) {
                    return
                }
            }
        }
        if (connectionNeeded > 0 && connectedPeers.isNotEmpty()) {
            knownPeers.clear()
            connectedPeers.keys.shuffled(SecureRandom())[0].writeAndFlush(createGetPeersMessage())
        }
    }

    override fun broadcast(packet: CommunicationProtocol.Packet) {
        connectedPeers.keys.forEach {
            it.writeAndFlush(packet)
        }
    }

    override fun addConnectedPeer(channel : Channel, peer: Peer) {
        connectedPeers[channel] = peer
    }

    override fun removeConnectedPeer(channel: Channel) : Peer? {
        return connectedPeers.remove(channel)
    }

    override fun connectedPeers() : Set<Peer> {
        val peers = mutableSetOf<Peer>()
        peers.addAll(connectedPeers.values)
        return peers
    }

    override fun addKnownPeers(peers: List<CommunicationProtocol.Peer>) {
        peers.forEach {
            knownPeers.add(Peer(it.networkId, it.host, it.port))
        }
    }

    override fun isConnected(networkId : String) : Boolean {
        for (peer in connectedPeers.values) {
            if (peer.networkId == networkId) {
                return true
            }
        }
        return false
    }

    override fun getNetworkId() : String {
        return networkId
    }

    override fun getOwnPeerInfo(): Peer {
        return Peer(networkId, properties.host!!, properties.port!!)
    }

    private fun createGetPeersMessage() : CommunicationProtocol.Packet{
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.GET_PEERS)
            .setGetPeers(CommunicationProtocol.GetPeers.newBuilder()
                .build())
            .build()
    }

}
