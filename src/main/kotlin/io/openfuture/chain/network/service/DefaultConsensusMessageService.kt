package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import org.springframework.stereotype.Component

//TODO: call consensus API here
@Component
class DefaultConsensusMessageService : ConsensusMessageService {

    override fun onBlockApproval(ctx: ChannelHandlerContext, block: BlockApprovalMessage) {}

    override fun onPendingBlock(ctx: ChannelHandlerContext, block: PendingBlockMessage) {}

}