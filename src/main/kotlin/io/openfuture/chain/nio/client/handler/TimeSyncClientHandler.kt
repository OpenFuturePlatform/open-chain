package io.openfuture.chain.nio.client.handler

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.util.NodeTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@ChannelHandler.Sharable
class TimeSyncClientHandler(
        private val time : NodeTime
) : BaseHandler(Type.TIME_RESPONSE){

    @Volatile
    private var connections: Set<Channel> = mutableSetOf()

    private val peerTimeOffsets: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    private val timeSyncThread: Thread = Thread(TimeSyncTask(60*1000, 10*1000))

    companion object {
        private val log = LoggerFactory.getLogger(TimeSyncClientHandler::class.java)
    }

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
            connections = connections.plus(ctx.channel())
            log.info("Connection with {} established", address)
        } else {
            log.error("Connection with {} rejected", address)
            ctx.close()
        }

        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        connections = connections.minus(ctx.channel())
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: Packet) {
        log.info("Time packet received from ${ctx.channel().remoteAddress()}")
        val duration = time.now() - message.timeResponse.initialTimestamp
        val offset = message.timeResponse.timestamp -
                (message.timeResponse.initialTimestamp + duration/2)

        peerTimeOffsets[ctx.channel().remoteAddress().toString()] = offset
    }

    private fun check(ctx: ChannelHandlerContext): Boolean {
        log.trace("Check {}", ctx.channel().remoteAddress())
        return true
    }

    inner class TimeSyncTask(
            private val period: Long,
            private val timeToWaitPackets: Long
    ): Runnable {

        override fun run() {
            log.info("Time sync thread is started")
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(period)

                    peerTimeOffsets.clear()

                    val time = time.now()
                    val request = CommunicationProtocol.Packet.newBuilder()
                            .setType(Type.TIME_REQUEST)
                            .setTimeRequest(TimeRequest.newBuilder().setTimestamp(time).build())
                            .build()
                    connections.forEach { it.writeAndFlush(request) }

                    log.info("Time packets were sent to peers $connections")

                    Thread(TimeCorrectionTask(timeToWaitPackets)).start()
                } catch (e :InterruptedException){
                    Thread.currentThread().interrupt()
                }
            }
        }

    }

    inner class TimeCorrectionTask(
            private val timeToWaitPackets: Long
    ): Runnable {

        override fun run() {
            Thread.sleep(timeToWaitPackets)

            log.info("Time correction started. Peers time offsets $peerTimeOffsets")

            val packetNumber = peerTimeOffsets.size
            if (packetNumber != 0 && (packetNumber == connections.size)){
                val offsets = ArrayList(peerTimeOffsets.values)
                offsets.sort()
                val mediumIndex = if (packetNumber % 2 == 0) packetNumber/2-1 else packetNumber/2
                val adjustment = offsets[mediumIndex]
                time.setAdjustment(adjustment)

                log.info("Time correction was successful. Adjustment is $adjustment")
            } else {
                log.info("Not all packets were received from peers. Can not do sync now.")
            }
        }

    }
}