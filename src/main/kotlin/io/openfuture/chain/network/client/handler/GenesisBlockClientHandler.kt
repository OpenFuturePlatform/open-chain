package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.network.domain.NetworkDelegate
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class GenesisBlockClientHandler(
    private val pendingBlockHandler: PendingBlockHandler
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
        pendingBlockHandler.addBlock(block)
    }

    private fun toActiveDelegates(networkDelegates: MutableSet<NetworkDelegate>): Set<Delegate>
        = networkDelegates.map { Delegate(it.publicKey, it.address) }.toSet()

}