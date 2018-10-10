package io.openfuture.chain.core.sync

import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.NOT_SYNCHRONIZED
import org.springframework.stereotype.Component

@Component
class SyncStatus {

    @Volatile
    private var syncStatus: SyncStatusType = NOT_SYNCHRONIZED


    @Synchronized
    fun getSyncStatus(): SyncStatusType = syncStatus

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
