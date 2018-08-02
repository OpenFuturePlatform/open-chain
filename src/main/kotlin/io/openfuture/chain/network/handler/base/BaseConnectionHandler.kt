package io.openfuture.chain.network.handler.base

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.message.TimeMessage
import io.openfuture.chain.network.message.application.block.BlockRequestMessage
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import io.openfuture.chain.network.message.application.block.MainBlockMessage
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.Packet
import io.openfuture.chain.network.message.network.PacketType.*
import io.openfuture.chain.network.message.network.address.AddressesMessage
import io.openfuture.chain.network.message.network.address.FindAddressesMessage
import io.openfuture.chain.network.message.network.time.AskTimeMessage
import io.openfuture.chain.network.service.message.*
import org.slf4j.LoggerFactory

abstract class BaseConnectionHandler(
    protected val greetingService: GreetingMessageService,
    protected val addressService: AddressDiscoveryMessageService,
    protected val heartBeatService: HeartBeatMessageService,
    protected val timeSyncService: TimeSyncMessageService,
    protected val blockService: BlockMessageService
) : SimpleChannelInboundHandler<Packet>() {

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        greetingService.onChannelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        when(packet.type) {
            ADDRESSES -> addressService.onAddresses(ctx, packet.data as AddressesMessage)
            FIND_ADDRESSES -> addressService.onFindAddresses(ctx, packet.data as FindAddressesMessage)
            GREETING -> greetingService.onGreeting(ctx, packet.data as GreetingMessage)
            HEART_BEAT -> heartBeatService.onHeartBeat(ctx, packet.data as HeartBeatMessage)
            TIME -> timeSyncService.onTime(ctx, packet.data as TimeMessage)
            MAIN_BLOCK -> blockService.onMainBlock(ctx, packet.data as MainBlockMessage)
            GENESIS_BLOCK -> blockService.onGenesisBlock(ctx, packet.data as GenesisBlockMessage)
            ASK_TIME -> timeSyncService.onAskTime(ctx, packet.data as AskTimeMessage)
            SYNC_BLOCKS_REQUEST -> blockService.onNetworkBlockRequest(ctx, packet.data as BlockRequestMessage)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        greetingService.onChannelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error $cause")
        ctx.close()
    }

}