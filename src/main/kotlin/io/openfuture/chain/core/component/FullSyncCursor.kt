package io.openfuture.chain.core.component

import org.springframework.stereotype.Component

@Component
class FullSyncCursor {
    var cursor: Long? = null
}