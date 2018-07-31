package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.network.domain.NetworkBlockApprovalMessage

interface PendingBlockHandler {

    fun addBlock(block: Block)

    fun handleApproveMessage(message: NetworkBlockApprovalMessage)

}