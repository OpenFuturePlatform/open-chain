package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.core.model.entity.base.Dictionary

enum class BlockApprovalStage(val value: Int) : Dictionary {
    IDLE(1),
    PREPARE(2),
    COMMIT(3);

    override fun getId(): Int = value

}