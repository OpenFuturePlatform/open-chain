package io.openfuture.chain.network.component

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import io.netty.channel.group.ChannelGroup
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.exception.NotFoundChannelException
import io.openfuture.chain.network.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChannelsHolder(
    private val channelGroup: ChannelGroup
) {

    companion object {
        private val log = LoggerFactory.getLogger(ChannelsHolder::class.java)
    }

    private val channelsAddresses = mutableMapOf<ChannelId, NetworkAddress>()


    fun broadcast(message: Serializable) {
        channelGroup.writeAndFlush(message)
    }

    fun sendRandom(message: Serializable) {
        val channel = getChannels().shuffled().firstOrNull()
            ?: throw NotFoundChannelException("List channels is empty")

        channel.writeAndFlush(message)
    }

    fun send(message: Serializable, address: NetworkAddress): Boolean {
        val channelId = channelsAddresses.filter { it.value == address }.keys.firstOrNull() ?: return false
        val channel = getChannels().first { it.id() == channelId }
        channel.writeAndFlush(message)
        return true
    }

    fun getChannels(): List<Channel> = channelGroup.toList()

    fun getAddresses(): List<NetworkAddress> = channelGroup.map { channelsAddresses[it.id()]!! }

    fun getAddressByChannelId(channelId: ChannelId): NetworkAddress? = channelsAddresses[channelId]

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

        log.info("Connection with ${channel.remoteAddress()} closed, connections count is ${channelGroup.size}")
    }

}