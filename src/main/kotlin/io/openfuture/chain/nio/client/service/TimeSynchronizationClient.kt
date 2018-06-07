package io.openfuture.chain.nio.client.service

import io.netty.channel.Channel
import io.openfuture.chain.message.TimeSynchronization
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName.TIME_SYNCHRONIZATION
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimeSynchronizationClient : ProtobufClient<TimeSynchronization.TimeSynchronizationMessage> {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun requestTime(channel : Channel){
        val servicePayloadBuilder = TimeSynchronization.TimeSynchronizationMessage.newBuilder()
        val servicePayload = servicePayloadBuilder.setRequestTime(System.currentTimeMillis()).build()
        val messageBuilder = CommunicationProtocolOuterClass.CommunicationProtocol.newBuilder()
        val message = messageBuilder
                .setServiceName(TIME_SYNCHRONIZATION)
                .setServicePayload(servicePayload.toByteString())
                .build()

        channel.writeAndFlush(message)
    }

    override fun handleResponse(message: TimeSynchronization.TimeSynchronizationMessage){
        log.info("Request time: ${message.requestTime}, response time: ${message.responseTime}, " +
                "difference: " + "${message.responseTime - message.requestTime}")
    }

    override fun canHandleResponse(serviceName: CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName)
            : Boolean{
        return serviceName == TIME_SYNCHRONIZATION
    }

    override fun takeMessageFromPacket(packet: CommunicationProtocolOuterClass.CommunicationProtocol): TimeSynchronization.TimeSynchronizationMessage {
        return TimeSynchronization.TimeSynchronizationMessage.parseFrom(packet.servicePayload)
    }
}