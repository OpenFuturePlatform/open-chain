package io.openfuture.chain.core.component

import io.openfuture.chain.core.model.entity.block.Block
import org.springframework.stereotype.Component

@Component
class SyncCursor {
    lateinit var fullCursor: Block
}