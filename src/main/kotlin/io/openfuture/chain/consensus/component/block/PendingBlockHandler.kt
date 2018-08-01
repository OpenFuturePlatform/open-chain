package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.domain.NetworkBlockApproval

interface PendingBlockHandler {

    fun addBlock(block: MainBlock)

    fun handleApproveMessage(message: NetworkBlockApproval)

}