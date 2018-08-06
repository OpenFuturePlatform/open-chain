package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import org.springframework.stereotype.Service

//TODO: call consensus API here
@Service
class DefaultConsensusMessageService : ConsensusMessageService {

    override fun onBlockApproval(ctx: ChannelHandlerContext, block: BlockApprovalMessage) {}

    override fun onPendingBlock(ctx: ChannelHandlerContext, block: PendingBlockMessage) {}

}