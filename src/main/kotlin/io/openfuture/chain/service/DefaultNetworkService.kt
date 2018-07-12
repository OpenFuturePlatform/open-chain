package io.openfuture.chain.service

import io.netty.bootstrap.Bootstrap
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

    companion object {
        private val log = LoggerFactory.getLogger(DefaultNetworkService::class.java)
    }


    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        // Start Server
        Executors.newSingleThreadExecutor().execute(tcpServer)

        // Start Clients
        val address = properties.rootNodes.shuffled(SecureRandom()).first().split(":")
        clientBootstrap.connect(address[0], address[1].toInt()).addListener { future ->
            future as ChannelFuture
            if (future.isSuccess) {
                future.channel().writeAndFlush(createFindPeersMessage())
            } else {
                log.warn("Can not connect to ${address[0]}:${address[1]}")
            }
        }
    }

    override fun broadcast(packet: CommunicationProtocol.Packet) {
        connectedPeers.keys.forEach {
            it.writeAndFlush(packet)
        }
    }

    @Scheduled(cron="*/30 * * * * *")
    override fun maintainConnectionNumber() {
        if (isConnectionNeeded()) {
            findPeers()
        }
    }

    override fun addPeer(channel : Channel, peer: Peer) {
        connectedPeers[channel] = peer
    }

    override fun removePeer(channel: Channel) : Peer? {
        return connectedPeers.remove(channel)
    }

    override fun getPeers() : Set<Peer> {
        val peers = mutableSetOf<Peer>()
        peers.addAll(connectedPeers.values)
        return peers
    }

    override fun connect(peers: List<CommunicationProtocol.Peer>) {
        peers.map { Peer(it.host, it.port) }
            .filter { !connectedPeers.values.contains(it) && it != Peer(properties.host!!, properties.port!!) }
            .forEach { clientBootstrap.connect(it.host, it.port) }
    }

    private fun createFindPeersMessage() : CommunicationProtocol.Packet{
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.FIND_PEERS)
            .setFindPeers(CommunicationProtocol.FindPeers.newBuilder()
                .build())
            .build()
    }

    private fun isConnectionNeeded(): Boolean = properties.peersNumber!! > connectedPeers.size

    private fun findPeers() {
        val message = createFindPeersMessage()

        val peer = connectedPeers.values.shuffled(SecureRandom()).firstOrNull() ?:
        properties.getRootPeers().shuffled().first()

        send(peer, message)
    }

    private fun send(peer: Peer, message: CommunicationProtocol.Packet) {
        var channel = connectedPeers.filter { it -> it.value == peer }.map { it -> it.key }.firstOrNull()
        if (channel == null) {
            channel = clientBootstrap.connect(peer.host, peer.port).channel()
        }
        channel!!.writeAndFlush(message)
    }

}
