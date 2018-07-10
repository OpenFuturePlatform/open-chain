package io.openfuture.chain.service

import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Service
import java.nio.channels.Channel

//This class will be implement by another developer
@Service
class DefaultNetworkService : NetworkService {

    override fun joinNetwork(host: String, port: Int) {
        throw UnsupportedOperationException()
    }

    override fun handleJoinResponse(message: CommunicationProtocol.Packet, channel: Channel) {
        throw UnsupportedOperationException()
    }

    override fun connect(host: String, port: Int) {
        throw UnsupportedOperationException()
    }

    override fun broadcast(packet: CommunicationProtocol.Packet) {
        throw UnsupportedOperationException()
    }

}