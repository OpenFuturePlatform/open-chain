package io.openfuture.chain.service

import io.openfuture.chain.nio.client.ClientChannels
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.util.NodeTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap

@Component
class TimeSyncService(
        private val time: NodeTime,
        private val properties: NodeProperties,
        private val channels: ClientChannels
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(TimeSyncService::class.java)

        /** This constant determine delay when we need to calculate time adjustment.
         *  Time adjustment calculation is based on time offsets that are populated when
         *  we receive time from other nodes. It means that we will need to wait response from
         *  other nodes. This time should be enough to send and receive packet from opposite
         *  side of the Earth */
        const val MILLISECONDS_TO_WAIT_FOR_ALL_OFFSETS : Long = 1000
    }

    private val nodeTimeOffsets: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    fun addTimeOffset(packet: CommunicationProtocol.Packet, remoteAddress: String){
        val roundTripTime = time.now() - packet.timeResponse.initialTimestamp
        val offset = packet.timeResponse.timestamp -
                (packet.timeResponse.initialTimestamp + roundTripTime/2)
        nodeTimeOffsets[remoteAddress] = offset
    }

    override fun run() {
        log.info("Time sync service started execution")

        while (!Thread.interrupted()) {
            try {
                nodeTimeOffsets.clear()
                requestTimeFromNodesToGetOffsets()

                Thread.sleep(MILLISECONDS_TO_WAIT_FOR_ALL_OFFSETS)
                adjustTimeBasedOnOffsets()

                Thread.sleep(properties.timeSyncInterval!! - MILLISECONDS_TO_WAIT_FOR_ALL_OFFSETS)
            } catch (e: InterruptedException){
                break
            }
        }

        log.info("Time sync service finished execution")
    }

    private fun requestTimeFromNodesToGetOffsets(){
        val request = CommunicationProtocol.Packet.newBuilder()
                .setType(CommunicationProtocol.Type.TIME_REQUEST)
                .setTimeRequest(CommunicationProtocol.TimeRequest.newBuilder().setTimestamp(time.now()).build())
                .build()
        channels.sendPacket(request)

        log.info("Time packets were sent to nodes ${channels.remoteAddresses()}")
    }

    private fun adjustTimeBasedOnOffsets(){
        log.info("Time adjustment started. Nodes time offsets $nodeTimeOffsets")

        val offsetsNumber = nodeTimeOffsets.size
        if (offsetsNumber != 0 && (offsetsNumber == channels.size())){
            val offsets = ArrayList(nodeTimeOffsets.values)
            offsets.sort()
            val medianIndex = if (offsetsNumber % 2 == 0) offsetsNumber/2-1 else offsetsNumber/2
            val adjustment = time.addAdjustment(offsets[medianIndex])

            log.info("Time adjustment was successful. Adjustment is $adjustment")
        } else {
            log.info("Not all packets were received from nodes. Can not do sync now.")
        }
    }
}