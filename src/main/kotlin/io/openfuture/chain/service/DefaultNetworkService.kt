package io.openfuture.chain.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.network.server.TcpServer
import io.openfuture.chain.property.NodeProperty
import io.openfuture.chain.protocol.CommunicationProtocol
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Service
class DefaultNetworkService(
    private val clientBootstrap: Bootstrap,
    private val tcpServer: TcpServer,
    private val property: NodeProperty
) : NetworkService, ApplicationListener<ApplicationReadyEvent> {

    private val connections: MutableMap<Channel, NetworkAddress> = ConcurrentHashMap()

    companion object {
        private val log = LoggerFactory.getLogger(DefaultNetworkService::class.java)
    }


    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        // Start Server
        Executors.newSingleThreadExecutor().execute(tcpServer)

        // Start Clients
        val address = property.getRootAddresses().shuffled(SecureRandom()).first()
        clientBootstrap.connect(address.host, address.port).addListener { future ->
            future as ChannelFuture
            if (future.isSuccess) {
                future.channel().writeAndFlush(createFindAddressMessage())
            } else {
                log.warn("Can not connect to ${address.host}:${address.port}")
            }
        }
    }

    override fun broadcast(packet: CommunicationProtocol.Packet) {
        connections.keys.forEach {
            it.writeAndFlush(packet)
        }
    }

    @Scheduled(cron = "*/30 * * * * *")
    override fun maintainConnectionNumber() {
        if (isConnectionNeeded()) {
            requestAddresses()
        }
    }

    override fun addConnection(channel: Channel, networkAddress: NetworkAddress) {
        connections[channel] = networkAddress
    }

    override fun removeConnection(channel: Channel): NetworkAddress? = connections.remove(channel)

    override fun getConnections(): Set<NetworkAddress> = connections.values.toSet()

    override fun connect(peers: List<CommunicationProtocol.NetworkAddress>) {
        peers.map { NetworkAddress(it.host, it.port) }
            .filter { !connections.values.contains(it) && it != NetworkAddress(property.host!!, property.port!!) }
            .forEach { clientBootstrap.connect(it.host, it.port) }
    }

    private fun createFindAddressMessage(): CommunicationProtocol.Packet {
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.ADDRESSES)
            .setFindAddresses(CommunicationProtocol.FindAddresses.newBuilder().build())
            .build()
    }

    private fun isConnectionNeeded(): Boolean = property.peersNumber!! > connections.size

    private fun requestAddresses() {
        val address = connections.values.shuffled(SecureRandom()).firstOrNull()
            ?: property.getRootAddresses().shuffled().first()

        send(address, createFindAddressMessage())
    }

    private fun send(networkAddress: NetworkAddress, message: CommunicationProtocol.Packet) {
        var channel = connections.filter { it -> it.value == networkAddress }.map { it -> it.key }.firstOrNull()
        if (channel == null) {
            channel = clientBootstrap.connect(networkAddress.host, networkAddress.port).channel()
        }
        channel!!.writeAndFlush(message)
    }

}
