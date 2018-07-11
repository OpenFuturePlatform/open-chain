package io.openfuture.chain.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.nio.ChannelAttributes
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocol
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

@Component
class DefaultNetworkService(
    private val clientBootstrap: Bootstrap,
    private val properties: NodeProperties,
    private val peerService: PeerService
) : NetworkService {

    private val inboundChannels : MutableSet<Channel> = ConcurrentHashMap.newKeySet()

    private val outboundChannels : MutableSet<Channel> = ConcurrentHashMap.newKeySet()

    @Volatile
    private var networkId : String? = null

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultNetworkService::class.java)
        private const val INBOUND_CONNECTION_NUMBER = 8
    }

    @EventListener
    override fun start(event: ApplicationReadyEvent) {
        peerService.deleteAll()

        val address = properties.rootNodes[0].split(":")
        clientBootstrap.connect(address[0], address[1].toInt()).addListener { future ->
            future as ChannelFuture
            if (future.isSuccess) {
                future.channel().writeAndFlush(createJoinNetworkMessage())
            } else {
                logger.warn("Can not join to network: ${address[0]} : ${address[1]}.")
            }
        }
    }

    @Scheduled(cron="*/30 * * * * *")
    override fun maintainInboundConnections() {
        if (networkId == null) {
            return
        }
        val connectionNeeded = INBOUND_CONNECTION_NUMBER - inboundChannels.size
        val peers = peerService.findAll().shuffled(SecureRandom())
        for (peer in peers) {
            if (peer.networkId != networkId && !isConnected(networkId!!)) {
                connect(peer.host, peer.port)
            }
            connectionNeeded.dec()
            if (connectionNeeded == 0) {
                return
            }
        }
    }

    override fun connect(host: String, port: Int) {
        clientBootstrap.connect(host, port).addListener { future ->
            future as ChannelFuture
            if (!future.isSuccess) {
                logger.warn("Can not connect to: $host : $port.")
            }
        }
    }

    override fun disconnect(channel: Channel) {
        channel.writeAndFlush(CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.DISCONNECT)
            .setDisconnect(CommunicationProtocol.Disconnect.newBuilder()
                .setLeaveNetwork(false)
                .build())
            .build())
        channel.close()
    }

    override fun broadcast(packet: CommunicationProtocol.Packet) {
        inboundChannels.plus(outboundChannels).forEach {
            it.writeAndFlush(packet)
        }
    }

    override fun activeInboundChannels() : MutableSet<Channel> {
        return inboundChannels
    }

    override fun activeOutboundChannels() : MutableSet<Channel> {
        return outboundChannels
    }

    override fun isConnected(networkId : String) : Boolean {
        for (channel in inboundChannels.plus(outboundChannels)) {
            val id = channel.attr(ChannelAttributes.REMOTE_NETWORK_ID).get()
            if (id == networkId) {
                return true
            }
        }
        return false
    }

    override fun getNetworkId() : String? {
        return networkId
    }

    override fun setNetworkId(networkId : String) {
        this.networkId = networkId
    }

    private fun createJoinNetworkMessage() : CommunicationProtocol.Packet{
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.JOIN_NETWORK_REQUEST)
            .setJoinNetworkRequest(CommunicationProtocol.JoinNetworkRequest.newBuilder()
                    .setPort(properties.port!!)
                    .build())
            .build()
    }

}
