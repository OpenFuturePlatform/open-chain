package io.openfuture.chain.nio

import io.netty.channel.Channel
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ChannelStorage {
    private val channels : MutableSet<Channel> = ConcurrentHashMap.newKeySet()

    fun add(channel : Channel) {
        channels.add(channel)
    }

    fun remove(channel : Channel) {
        channels.remove(channel)
    }

    fun writeAndFlush(packet: CommunicationProtocol.Packet) {
        channels.forEach {
            it.writeAndFlush(packet)
        }
    }
}