package io.openfuture.chain.network.component

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.exception.NotFoundChannelException
import io.openfuture.chain.network.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ChannelsHolder {

    companion object {
        private val log = LoggerFactory.getLogger(ChannelsHolder::class.java)
    }

    private var channelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    private var nodesInfo = ConcurrentHashMap<ChannelId, NodeInfo>()


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
            ?: throw NotFoundChannelException("Channel with uid: ${nodeInfo.uid} is not exist")

        channel.writeAndFlush(message)
        return true
    }

    fun size(): Int = channelGroup.size

    fun isEmpty(): Boolean = channelGroup.isEmpty()

    fun getNodesInfo(): List<NodeInfo> = channelGroup.map { nodesInfo[it.id()]!! }

    fun getNodeInfoByChannelId(channelId: ChannelId): NodeInfo = nodesInfo[channelId]!!

    @Synchronized
    fun addChannel(channel: Channel, nodeInfo: NodeInfo) {
        channelGroup.add(channel)
        nodesInfo[channel.id()] = nodeInfo

        log.info("Connection with ${channel.remoteAddress()} established, connections count is ${channelGroup.size}")
    }

    @Synchronized
    fun removeChannel(channel: Channel) {
        channelGroup.remove(channel)
        nodesInfo.remove(channel.id())

        channel.close()

        log.info("Connection with ${channel.remoteAddress()} closed, connections count is ${channelGroup.size}")
    }

}