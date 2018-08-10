package io.openfuture.chain.network.sync

import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SynchronizationStatus.NOT_SYNCHRONIZED
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