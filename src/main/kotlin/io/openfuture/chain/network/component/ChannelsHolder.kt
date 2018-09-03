package io.openfuture.chain.network.component

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import io.netty.channel.group.ChannelGroup
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.exception.NotFoundChannelException
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ChannelsHolder(
    @Qualifier("serverChannelGroup") private val serverChannelGroup: ChannelGroup,
    @Qualifier("peerChannelGroup") private val peerChannelGroup: ChannelGroup
) {

    private val channelsAddresses = ConcurrentHashMap<ChannelId, NetworkAddress>()

    fun broadcast(message: Serializable) {
        peerChannelGroup.writeAndFlush(message)
        serverChannelGroup.writeAndFlush(message)
    }

    fun send(message: Serializable) {
        val channel = getAllChannels().shuffled().firstOrNull()
            ?: throw NotFoundChannelException("List channels is empty")

        channel.writeAndFlush(message)
    }

    fun send(message: Serializable, address: NetworkAddress): Boolean {
        val channelId = channelsAddresses.filter { it.value == address }.keys.firstOrNull() ?: return false
        val channel = getAllChannels().first { it.id() == channelId }
        channel.writeAndFlush(message)
        return true
    }

    fun getChannelsCount(): Int = peerChannelGroup.size + serverChannelGroup.size

    fun isChannelsEmpty(): Boolean = peerChannelGroup.isEmpty() && serverChannelGroup.isEmpty()

    fun getAllChannels(): List<Channel> = peerChannelGroup.toList() + serverChannelGroup.toList()

    fun getAddressByChannelId(channelId: ChannelId): NetworkAddress = channelsAddresses[channelId]!!

    fun getClientsAddresses(): List<NetworkAddress> = serverChannelGroup.map { channelsAddresses[it.id()]!! }

    fun getPeersAddresses(): List<NetworkAddress> = peerChannelGroup.map { channelsAddresses[it.id()]!! }

    fun getAllAddresses(): List<NetworkAddress> = getPeersAddresses() + getClientsAddresses()

    fun addPeer(channel: Channel, networkAddress: NetworkAddress) {
        peerChannelGroup.add(channel)
        channelsAddresses[channel.id()] = networkAddress
    }

    fun addClient(channel: Channel, networkAddress: NetworkAddress) {
        serverChannelGroup.add(channel)
        channelsAddresses[channel.id()] = networkAddress
    }

    fun removeChannel(channel: Channel) {
        if (!serverChannelGroup.remove(channel)) {
            peerChannelGroup.remove(channel)
        }
        channelsAddresses.remove(channel.id())
    }

}