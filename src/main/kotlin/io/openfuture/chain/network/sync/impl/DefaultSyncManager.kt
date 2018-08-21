package io.openfuture.chain.network.sync.impl

import io.openfuture.chain.network.sync.SyncManager
import org.springframework.stereotype.Component

@Component
class DefaultSyncManager: SyncManager {

    @Volatile
    private var syncStatus: SynchronizationStatus = SynchronizationStatus.NOT_SYNCHRONIZED


    @Synchronized
    override fun getSyncStatus(): SynchronizationStatus {
        return syncStatus
    }

    @Synchronized
    override fun setSyncStatus(status: SynchronizationStatus) {
        this.syncStatus = status
    }

}