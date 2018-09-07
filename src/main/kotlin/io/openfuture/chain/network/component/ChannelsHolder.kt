package io.openfuture.chain.network.component

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.network.entity.NetworkAddress
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
    private var channelsAddresses = ConcurrentHashMap<ChannelId, NetworkAddress>()


    fun broadcast(message: Serializable) {
        channelGroup.writeAndFlush(message)
    }

    fun sendRandom(message: Serializable) {
        val channel = channelGroup.shuffled().firstOrNull()
            ?: throw NotFoundChannelException("List channels is empty")

        channel.writeAndFlush(message)
    }

    fun send(message: Serializable, address: NetworkAddress): Boolean {
        val channelId = channelsAddresses.filter { it.value == address }.keys.firstOrNull() ?: return false
        val channel = channelGroup.firstOrNull { it.id() == channelId }
            ?: throw NotFoundChannelException("Channel with address: ${address.host}:${address.port} is not exist")

        channel.writeAndFlush(message)
        return true
    }

    fun size(): Int = channelGroup.size

    fun isEmpty(): Boolean = channelGroup.isEmpty()

    fun getAddresses(): List<NetworkAddress> = channelGroup.map { channelsAddresses[it.id()]!! }

    fun getAddressByChannelId(channelId: ChannelId): NetworkAddress = channelsAddresses[channelId]!!

    @Synchronized
    fun addChannel(channel: Channel, networkAddress: NetworkAddress) {
        channelGroup.add(channel)
        channelsAddresses[channel.id()] = networkAddress

        log.info("Connection with ${channel.remoteAddress()} established, connections count is ${channelGroup.size}")
    }

    @Synchronized
    fun removeChannel(channel: Channel) {
        channelGroup.remove(channel)
        channelsAddresses.remove(channel.id())

        channel.close()

        log.info("Connection with ${channel.remoteAddress()} closed, connections count is ${channelGroup.size}")
    }

}