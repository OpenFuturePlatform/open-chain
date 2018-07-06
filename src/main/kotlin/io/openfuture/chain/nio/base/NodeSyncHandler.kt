package io.openfuture.chain.nio.base

import com.google.protobuf.ByteString
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Node
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.repository.NodeRepository
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class NodeSyncHandler(
    private val repository: NodeRepository
) : BaseHandler(CommunicationProtocol.Type.NODE_SYNC) {

    override fun channelActive(ctx: ChannelHandlerContext) {
        val message = CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.NODE_SYNC)
            .setNodeSync(CommunicationProtocol.NodeSync.newBuilder()
                .addAllNode(createNodeMessages(repository.findAll()))
                .build())
            .build()
        ctx.writeAndFlush(message)
        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val peerNodes = createNodeEntities(message)
        val ownNodes = repository.findAll()
        repository.saveAll(peerNodes.minus(ownNodes))
    }

    private fun createNodeMessages(nodes : List<Node>) : List<CommunicationProtocol.Node> {
        val result = ArrayList<CommunicationProtocol.Node>(nodes.size)
        nodes.forEach {
            result.add(CommunicationProtocol.Node.newBuilder()
                .setPublicKey(ByteString.copyFrom(it.publicKey))
                .setHost(it.host)
                .setPort(it.port)
                .build())
        }
        return result
    }

    private fun createNodeEntities(message: CommunicationProtocol.Packet) : List<Node> {
        val nodes = message.nodeSync.nodeList
        val result = ArrayList<Node>(nodes.size)
        nodes.forEach {
            result.add(Node(it.publicKey.toByteArray(), it.host, it.port))
        }
        return result
    }
}