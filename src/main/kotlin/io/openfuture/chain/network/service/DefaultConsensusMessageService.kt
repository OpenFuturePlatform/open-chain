package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.application.block.BlockApprovalMessage
import io.openfuture.chain.network.message.application.block.PendingBlockMessage
import org.springframework.stereotype.Component

@Component
class DefaultConsensusMessageService : ConsensusMessageService {

    override fun onBlockApproval(ctx: ChannelHandlerContext, block: BlockApprovalMessage) {}

    override fun onPendingBlock(ctx: ChannelHandlerContext, block: PendingBlockMessage) {}

}