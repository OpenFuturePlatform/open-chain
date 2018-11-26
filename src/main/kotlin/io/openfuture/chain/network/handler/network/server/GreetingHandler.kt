package io.openfuture.chain.network.handler.network.server

import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.concurrent.GenericFutureListener
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.message.network.NewClient
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingHandler(
    private val nodeKeyHolder: NodeKeyHolder,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder,
    private val nodeProperties: NodeProperties
) : SimpleChannelInboundHandler<GreetingMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GreetingHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingMessage) {
        val hostAddress = (ctx.channel().remoteAddress() as InetSocketAddress).address.hostAddress
        val nodeInfo = NodeInfo(msg.uid, NetworkAddress(hostAddress, msg.externalPort))
        val nodesInfo = addressesHolder.getNodesInfo()
        val response = GreetingResponseMessage(nodeKeyHolder.getUid(), hostAddress, nodesInfo)
        if (isConnectionAcceptable(nodeInfo, ctx.channel())) {
            log.info("Accepted connection from ${ctx.channel().remoteAddress()} (Tachka ${msg.externalPort})")
            addressesHolder.addNodeInfo(nodeInfo)
            channelHolder.broadcast(NewClient(nodeInfo))
            ctx.writeAndFlush(response)
        } else {
            log.info("Rejected connection from ${ctx.channel().remoteAddress()} (Tachka ${msg.externalPort})")
            log.info("Is known: ${channelHolder.getNodesInfo().joinToString { it.address.port.toString() }} | ${channelHolder.size()})")
            response.accepted = false
            response.loop = (msg.uid == nodeKeyHolder.getUid())
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
        }
    }

    private fun isConnectionAcceptable(nodeInfo: NodeInfo, channel: Channel): Boolean {
        return nodeInfo.uid != nodeKeyHolder.getUid()
            && nodeProperties.allowedConnections!! > channelHolder.size()
            && channelHolder.addChannel(channel, nodeInfo)
    }
}