package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingResponseHandler(
    private val config: NodeConfigurator,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder,
    private val connectionService: ConnectionService,
    private val nodeProperties: NodeProperties
) : SimpleChannelInboundHandler<GreetingResponseMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GreetingResponseHandler::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingResponseMessage) {
        val channel = ctx.channel()
        val socket = channel.remoteAddress() as InetSocketAddress

        config.setExternalHost(msg.externalHost)
        val address = NetworkAddress(socket.address.hostAddress, socket.port)
        val nodeInfo = NodeInfo(msg.uid, address)
        if (msg.accepted) {
            log.error("Received accept from ${ctx.channel().remoteAddress()}")
            channelHolder.addChannel(channel, nodeInfo)
        } else {
            log.error("Received reject from ${ctx.channel().remoteAddress()}")
            if (msg.loop) {
                nodeProperties.setMyself(nodeInfo)
            } else {
                addressesHolder.markRejected(nodeInfo)
            }
            ctx.close()
        }
        addressesHolder.addNodesInfo(msg.nodesInfo)
        connectionService.findNewPeer()
    }

}