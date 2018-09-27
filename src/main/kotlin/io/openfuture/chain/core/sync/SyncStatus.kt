package io.openfuture.chain.core.sync

import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.NOT_SYNCHRONIZED
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.PROCESSING
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SyncStatus(
    private val eventPublisher: ApplicationEventPublisher
) {

    @Volatile
    private var syncStatus: SyncStatusType = NOT_SYNCHRONIZED


    @Synchronized
    fun getSyncStatus(): SyncStatusType = syncStatus

    @Synchronized
    fun setSyncStatus(status: SyncStatusType) {
        if (PROCESSING != status && status != syncStatus) {
            eventPublisher.publishEvent(status)
        }

        this.syncStatus = status
    }

    enum class SyncStatusType {
        SYNCHRONIZED,
        PROCESSING,
        NOT_SYNCHRONIZED
    }

}
