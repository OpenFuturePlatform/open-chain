package io.openfuture.chain.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.nio.NodeAttributes
import io.openfuture.chain.protocol.CommunicationProtocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class DefaultNetworkService(
    private val clientBootstrap: Bootstrap,
    private val nodeAttributes: NodeAttributes
) : NetworkService {

    private val channels : MutableSet<Channel> = ConcurrentHashMap.newKeySet()

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultNetworkService::class.java)
    }

    override fun join(host: String, port: Int) {
        clientBootstrap.connect(host, port).addListener { future ->
            future as ChannelFuture
            if (future.isSuccess) {
                future.channel().writeAndFlush(createJoinNetworkMessage())
            } else {
                logger.warn("Can not join to network: $host : $port.")
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
        channels.forEach {
            it.writeAndFlush(packet)
        }
    }

    override fun activeChannels() : MutableSet<Channel> {
        return channels
    }

    private fun createJoinNetworkMessage() : CommunicationProtocol.Packet{
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.JOIN_NETWORK_REQUEST)
            .setJoinNetworkRequest(CommunicationProtocol.JoinNetworkRequest.newBuilder()
                    .setPort(nodeAttributes.port)
                    .build())
            .build()
    }

}
