package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.core.MainBlockMessage

interface PendingBlockHandler {

    fun addBlock(block: MainBlockMessage)

    fun handleApproveMessage(message: BlockApprovalMessage)

    fun resetSlotNumber()

}