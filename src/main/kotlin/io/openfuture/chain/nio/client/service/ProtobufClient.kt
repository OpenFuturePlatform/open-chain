package io.openfuture.chain.nio.client.service

import com.google.protobuf.GeneratedMessageV3
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass

interface ProtobufClient<T : GeneratedMessageV3>{
    fun handleResponse(packet: CommunicationProtocolOuterClass.CommunicationProtocol){
        val message = takeMessageFromPacket(packet)
        handleResponse(message)
    }
    fun handleResponse(message: T)
    fun canHandleResponse(serviceName: CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName) : Boolean
    fun takeMessageFromPacket(packet: CommunicationProtocolOuterClass.CommunicationProtocol) : T
}