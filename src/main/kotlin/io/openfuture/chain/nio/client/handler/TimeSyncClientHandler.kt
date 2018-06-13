package io.openfuture.chain.nio.client.handler

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.util.NodeTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@ChannelHandler.Sharable
class TimeSyncClientHandler(
        private val time : NodeTime,
        private val properties: NodeProperties
) : BaseHandler(Type.TIME_RESPONSE){

    companion object {
        private val log = LoggerFactory.getLogger(TimeSyncClientHandler::class.java)
    }

    private val connections: MutableSet<Channel> = ConcurrentHashMap.newKeySet()

    private val nodeTimeOffsets: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    private val timeSyncThread: Thread = Thread(TimeSyncTask(properties.timeSyncInitialDelay!! * 1000,
            properties.timeSyncInterval!! * 1000, 1*1000))

    @PostConstruct
    fun init(){
        timeSyncThread.start()
    }

    @PreDestroy
    fun destroy(){
        timeSyncThread.interrupt()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val address = ctx.channel().remoteAddress()

        if (check(ctx)) {
            connections.add(ctx.channel())
            log.info("Connection with {} established", address)
        } else {
            log.error("Connection with {} rejected", address)
            ctx.close()
        }

        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        connections.remove(ctx.channel())
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: Packet) {
        log.info("Time packet received from ${ctx.channel().remoteAddress()}")
        val roundTripTime = time.now() - message.timeResponse.initialTimestamp
        val offset = message.timeResponse.timestamp -
                (message.timeResponse.initialTimestamp + roundTripTime/2)
        nodeTimeOffsets[ctx.channel().remoteAddress().toString()] = offset
    }

    private fun check(ctx: ChannelHandlerContext): Boolean {
        log.trace("Check {}", ctx.channel().remoteAddress())
        return true
    }

    inner class TimeSyncTask(
            private val initialDelay: Long,
            private val period: Long,
            private val offsetsWaitingTime: Long
    ): Runnable {

        override fun run() {
            log.info("Time sync thread started execution")

            Thread.sleep(initialDelay)
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(period)
                    nodeTimeOffsets.clear()
                    requestTimeFromNodesToGetOffsets()

                    Thread.sleep(offsetsWaitingTime)
                    adjustTimeBasedOnOffsets()
                } catch (e: InterruptedException){
                    break
                }
            }

            log.info("Time sync thread finished execution")
        }

        private fun requestTimeFromNodesToGetOffsets(){
            val request = CommunicationProtocol.Packet.newBuilder()
                    .setType(Type.TIME_REQUEST)
                    .setTimeRequest(TimeRequest.newBuilder().setTimestamp(time.now()).build())
                    .build()
            connections.forEach { it.writeAndFlush(request) }

            log.info("Time packets were sent to nodes $connections")
        }

        private fun adjustTimeBasedOnOffsets(){
            log.info("Time adjustment started. Nodes time offsets $nodeTimeOffsets")

            val offsetsNumber = nodeTimeOffsets.size
            if (offsetsNumber != 0 && (offsetsNumber == connections.size)){
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
}