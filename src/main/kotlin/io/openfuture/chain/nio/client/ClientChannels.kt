package io.openfuture.chain.nio.client

import io.netty.channel.Channel
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ClientChannels {

    private val channels: MutableSet<Channel> = ConcurrentHashMap.newKeySet()

    fun addChannel(channel: Channel){
        channels.add(channel)
    }

    fun removeChannel(channel: Channel){
        channels.remove(channel)
    }

    fun sendPacket(packet : CommunicationProtocol.Packet){
        channels.forEach { it.writeAndFlush(packet) }
    }

    fun remoteAddresses() : Set<String>{
        return channels.map { it.remoteAddress().toString() }.toSet()
    }

    fun size() : Int{
        return channels.size
    }
}