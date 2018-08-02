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
import io.openfuture.chain.network.message.network.PacketType
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
        greetingService.handleChannelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        when(packet.type) {
            PacketType.ADDRESSES -> addressService.handleAddressMessage(ctx, packet.data as AddressesMessage)
            PacketType.FIND_ADDRESSES -> addressService.handleFindAddressMessage(ctx, packet.data as FindAddressesMessage)
            PacketType.GREETING -> greetingService.handleGreetingMessage(ctx, packet.data as GreetingMessage)
            PacketType.HEART_BEAT -> heartBeatService.handleHeartBeatMessage(ctx, packet.data as HeartBeatMessage)
            PacketType.TIME -> timeSyncService.handleTimeMessage(ctx, packet.data as TimeMessage)
            PacketType.MAIN_BLOCK -> blockService.handleNetworkMainBlock(ctx, packet.data as MainBlockMessage)
            PacketType.GENESIS_BLOCK -> blockService.handleNetworkGenesisBlock(ctx, packet.data as GenesisBlockMessage)
            PacketType.ASK_TIME -> timeSyncService.handleAskTimeMessage(ctx, packet.data as AskTimeMessage)
            PacketType.SYNC_BLOCKS_REQUEST -> blockService.handleNetworkBlockRequest(ctx, packet.data as BlockRequestMessage)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        greetingService.handleChannelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error $cause")
        ctx.close()
    }

}