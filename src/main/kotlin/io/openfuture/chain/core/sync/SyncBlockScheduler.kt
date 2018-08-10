package io.openfuture.chain.core.sync

import io.openfuture.chain.core.sync.SynchronizationStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncBlockScheduler(
    private val syncBlockHandler: SyncBlockHandler,
    private val networkApiService: NetworkApiService
) {

    @Scheduled(fixedRateString = "\${node.synchronization-interval}")
    fun syncBlock() {
        if (syncBlockHandler.getSyncStatus() == NOT_SYNCHRONIZED && !networkApiService.isChannelsEmpty()) {
            syncBlockHandler.synchronize()
        }
    }

}