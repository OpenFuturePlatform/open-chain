package io.openfuture.chain.block

import io.openfuture.chain.consensus.component.block.ObserverStage

class BlockApprovalMessage(
    val stage: ObserverStage,
    val height: Long,
    val hash: String,
    val publicKey: String
)