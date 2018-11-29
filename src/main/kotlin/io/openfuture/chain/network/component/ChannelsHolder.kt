package io.openfuture.chain.network.component

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.exception.NotFoundChannelException
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ChannelsHolder {

    private val nodesInfo = ConcurrentHashMap<ChannelId, NodeInfo>()

    private val channelGroup = object : DefaultChannelGroup(GlobalEventExecutor.INSTANCE) {

        @Synchronized
        override fun remove(channel: Channel): Boolean {
            nodesInfo.remove(channel.id())
            return super.remove(channel)
        }

    }


    fun broadcast(message: Serializable) {
        channelGroup.writeAndFlush(message)
    }

    fun sendRandom(message: Serializable) {
        val channel = channelGroup.shuffled().firstOrNull()
            ?: throw NotFoundChannelException("List channels is empty")

        channel.writeAndFlush(message)
    }

    fun send(message: Serializable, nodeInfo: NodeInfo): Boolean {
        val channelId = nodesInfo.filter { it.value == nodeInfo }.keys.firstOrNull() ?: return false
        val channel = channelGroup.firstOrNull { it.id() == channelId }

        if (null == channel) {
            nodesInfo.remove(channelId)
            return false
        }

        channel.writeAndFlush(message)
        return true
    }

    fun size(): Int = channelGroup.size

    fun isEmpty(): Boolean = channelGroup.isEmpty()

    fun getNodesInfo(): List<NodeInfo> = channelGroup.map { nodesInfo[it.id()]!! }

    fun getNodeInfoByChannelId(channelId: ChannelId): NodeInfo? = nodesInfo[channelId]

    @Synchronized
    fun addChannel(channel: Channel, nodeInfo: NodeInfo): Boolean {
        if (nodesInfo.contains(nodeInfo)) {
            return false
        }
        nodesInfo[channel.id()] = nodeInfo
        channelGroup.add(channel)
        return true
    }

    @Synchronized
    fun hasChannel(channel: Channel): Boolean = channelGroup.contains(channel)

}