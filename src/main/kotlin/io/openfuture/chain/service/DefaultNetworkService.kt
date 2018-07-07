package io.openfuture.chain.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.nio.NodeAttributes
import io.openfuture.chain.protocol.CommunicationProtocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultNetworkService(
    private val clientBootstrap: Bootstrap,
    private val nodeService: NodeService,
    private val nodeAttributes: NodeAttributes
) : NetworkService {

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


    override fun handleJoinResponse(message: CommunicationProtocol.Packet,
                                    channel: Channel) {
        val payload = message.joinNetworkResponse
        nodeService.saveAll(payload.nodesList)
        nodeAttributes.host = payload.host
        nodeAttributes.networkId = payload.networkId
        channel.close()
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