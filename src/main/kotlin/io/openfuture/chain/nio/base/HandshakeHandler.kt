package io.openfuture.chain.nio.base

import com.google.protobuf.ByteString
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.entity.Node
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocol

open class HandshakeHandler(
    private val properties: NodeProperties
) : ChannelInboundHandlerAdapter(){

    protected fun sendHandshakeMessage(ctx: ChannelHandlerContext) {
        val message = CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.HANDSHAKE)
            .setHandshake(CommunicationProtocol.Handshake.newBuilder()
                .setNode(CommunicationProtocol.Node.newBuilder()
                    .setPublicKey(ByteString.copyFrom(properties.publicKey))
                    .setHost(properties.host)
                    .setPort(properties.port!!)
                    .build()))
            .build()

        ctx.writeAndFlush(message)
    }

    protected fun isValid(message: CommunicationProtocol.Handshake) : Boolean {
        val messagePublicKey = message.node.publicKey.toByteArray()
        return !properties.publicKey!!.contentEquals(messagePublicKey)
    }

    protected fun handleSuccessfulHandshake(ctx: ChannelHandlerContext,
                                            message: CommunicationProtocol.Handshake) {
        ctx.fireChannelActive()
        val node = Node(message.node.publicKey.toByteArray(), message.node.host, message.node.port)
        ctx.channel().attr(ChannelAttributes.NODE_KEY).set(node)
        ctx.pipeline().remove(this)
    }

    protected fun handleFailedHandshake(ctx: ChannelHandlerContext) {
        ctx.close()
    }
}