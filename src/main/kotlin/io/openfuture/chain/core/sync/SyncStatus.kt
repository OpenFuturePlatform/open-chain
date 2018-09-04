package io.openfuture.chain.core.sync

import org.springframework.stereotype.Component

@Component
class SyncStatus {

    @Volatile
    private var syncStatus: SyncStatusType = SyncStatusType.NOT_SYNCHRONIZED


    @Synchronized
    fun getSyncStatus(): SyncStatusType {
        return syncStatus
    }

    @Synchronized
    fun setSyncStatus(status: SyncStatusType) {
        this.syncStatus = status
    }

    enum class SyncStatusType {
        SYNCHRONIZED,
        PROCESSING,
        NOT_SYNCHRONIZED
    }

}
