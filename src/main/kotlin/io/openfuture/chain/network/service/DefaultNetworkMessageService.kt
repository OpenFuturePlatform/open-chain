package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.network.*
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.SECONDS

@Service
class DefaultNetworkMessageService(
    @Lazy
    private val connectionService: ConnectionService,
    private val properties: NodeProperties,
    private val clock: NodeClock
) : NetworkMessageService {

    private val heartBeatTasks: MutableMap<Channel, ScheduledFuture<*>> = ConcurrentHashMap()

    companion object {
        private const val HEART_BEAT_DELAY = 0L
        private const val HEART_BEAT_INTERVAL = 20L
    }


    override fun onChannelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(GreetingMessage(NetworkAddressMessage(properties.host!!, properties.port!!)))
    }

    override fun onClientChannelActive(ctx: ChannelHandlerContext) {
        val message = AskTimeMessage(clock.nodeTime())
        ctx.channel().writeAndFlush(message)

        val task = ctx.channel()
            .eventLoop()
            .scheduleAtFixedRate({ ctx.writeAndFlush(HeartBeatMessage(PING)) },
                HEART_BEAT_DELAY,
                HEART_BEAT_INTERVAL,
                SECONDS)
        heartBeatTasks[ctx.channel()] = task
    }

    override fun onHeartBeat(ctx: ChannelHandlerContext, heartBeat: HeartBeatMessage) {
        if (heartBeat.type == PING) {
            ctx.channel().writeAndFlush(HeartBeatMessage(PONG))
        }
    }

    override fun onFindAddresses(ctx: ChannelHandlerContext, message: FindAddressesMessage) {
        ctx.writeAndFlush(AddressesMessage(connectionService.getConnectionAddresses().toList()))
    }

    override fun onAddresses(ctx: ChannelHandlerContext, message: AddressesMessage) {
        connectionService.connect(message.values)
    }

    override fun onGreeting(ctx: ChannelHandlerContext, message: GreetingMessage) {
        connectionService.addConnection(ctx.channel(), message.address)
    }

    override fun onAskTime(ctx: ChannelHandlerContext, askTime: AskTimeMessage) {
        ctx.channel().writeAndFlush(TimeMessage(askTime.nodeTimestamp, clock.networkTime()))
    }

    override fun onTime(ctx: ChannelHandlerContext, message: TimeMessage) {
        val offset = clock.calculateTimeOffset(message.nodeTimestamp, message.networkTimestamp)
        clock.addTimeOffset(ctx.channel().remoteAddress().toString(), offset)
    }

    override fun onChannelInactive(ctx: ChannelHandlerContext) {
        clock.removeTimeOffset(ctx.channel().remoteAddress().toString())
    }

    override fun onClientChannelInactive(ctx: ChannelHandlerContext) {
        connectionService.removeConnection(ctx.channel())
        heartBeatTasks.remove(ctx.channel())!!.cancel(true)
    }

}
