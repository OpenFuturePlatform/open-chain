package io.openfuture.chain.core.sync

import io.openfuture.chain.core.sync.SynchronizationStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.network.service.NetworkInnerService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncBlockScheduler(
    private val syncBlockHandler: SyncBlockHandler,
    private val networkInnerService: NetworkInnerService
) {

    @Scheduled(fixedRateString = "\${node.synchronization-interval}")
    fun syncBlock() {
        if (syncBlockHandler.getSyncStatus() == NOT_SYNCHRONIZED && !networkInnerService.getChannels().isEmpty()) {
            syncBlockHandler.synchronize()
        }
    }

}