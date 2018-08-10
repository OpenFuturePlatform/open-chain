package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage

interface PendingBlockHandler {

    fun addBlock(block: PendingBlockMessage)

    fun handleApproveMessage(message: BlockApprovalMessage)

    fun resetSlotNumber()

}