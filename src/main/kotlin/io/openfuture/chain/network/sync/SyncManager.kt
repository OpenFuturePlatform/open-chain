package io.openfuture.chain.network.sync

import io.openfuture.chain.network.sync.impl.SynchronizationStatus

interface SyncManager {

    fun getSyncStatus(): SynchronizationStatus

    fun setSyncStatus(status: SynchronizationStatus)

}