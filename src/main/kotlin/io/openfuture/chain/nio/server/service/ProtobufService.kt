package io.openfuture.chain.nio.server.service

import com.google.protobuf.GeneratedMessageV3
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass

interface ProtobufService<T : GeneratedMessageV3> {
    fun canHandlePacket(serviceName : CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName)
            : Boolean

    fun handleMessage(packet : CommunicationProtocolOuterClass.CommunicationProtocol)
            : CommunicationProtocolOuterClass.CommunicationProtocol{
        val message = takeMessageFromPacket(packet)
        val serviceResponse = handleMessage(message)
        return updatePacketByMessage(packet, serviceResponse)
    }

    fun takeMessageFromPacket(packet : CommunicationProtocolOuterClass.CommunicationProtocol): T

    fun handleMessage(message: T) : T

    fun updatePacketByMessage(packet : CommunicationProtocolOuterClass.CommunicationProtocol, message : T)
            : CommunicationProtocolOuterClass.CommunicationProtocol
}