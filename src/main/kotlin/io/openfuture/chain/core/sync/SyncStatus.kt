package io.openfuture.chain.core.sync

enum class SyncStatus (val priority: Int) {
    SYNCHRONIZED(3),
    PROCESSING(2),
    NOT_SYNCHRONIZED(1)
}