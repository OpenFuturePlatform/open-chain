package io.openfuture.chain.nio.server.service

import io.openfuture.chain.message.TimeSynchronization
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName.TIME_SYNCHRONIZATION
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Vadzim Marchanka
 */
@Component
class TimeSynchronizationService : ProtobufService<TimeSynchronization.TimeSynchronizationMessage>{

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun handleMessage(message: TimeSynchronization.TimeSynchronizationMessage ) :
            TimeSynchronization.TimeSynchronizationMessage{
        log.info("Request was sent at : ${message.requestTime} milliseconds")
        return message.toBuilder().setResponseTime(System.currentTimeMillis()).build()
    }

    override fun canHandlePacket(serviceName : CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName) : Boolean{
        return serviceName == TIME_SYNCHRONIZATION
    }

    override fun takeMessageFromPacket(packet : CommunicationProtocolOuterClass.CommunicationProtocol)
            : TimeSynchronization.TimeSynchronizationMessage{
        return TimeSynchronization.TimeSynchronizationMessage.parseFrom(packet.servicePayload)
    }

    override fun updatePacketByMessage(
            packet : CommunicationProtocolOuterClass.CommunicationProtocol,
            message : TimeSynchronization.TimeSynchronizationMessage)
            : CommunicationProtocolOuterClass.CommunicationProtocol{
        return packet.toBuilder().setServicePayload(message.toByteString()).build()
    }
}