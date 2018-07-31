package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.consensus.component.block.BlockObserver
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.network.domain.NetworkDelegate
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class GenesisBlockClientHandler(
    private val blockObserver: BlockObserver
) : ClientHandler<NetworkGenesisBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, networkBlock: NetworkGenesisBlock) {
        val block = GenesisBlock(
            networkBlock.height,
            networkBlock.previousHash,
            networkBlock.timestamp!!,
            networkBlock.publicKey,
            networkBlock.epochIndex,
            toActiveDelegates(networkBlock.activeDelegates)
        )
        blockObserver.addBlock(block)
    }

    private fun toActiveDelegates(networkDelegates: MutableSet<NetworkDelegate>): Set<Delegate>
        = networkDelegates.map { Delegate(it.publicKey, it.address) }.toSet()

}