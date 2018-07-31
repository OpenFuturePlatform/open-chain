package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.block.BlockApprovalMessage
import io.openfuture.chain.core.model.entity.block.Block

interface PendingBlockHandler {

    fun addBlock(block: Block)

    fun handleApproveMessage(message: BlockApprovalMessage)

}