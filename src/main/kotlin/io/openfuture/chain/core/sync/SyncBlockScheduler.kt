package io.openfuture.chain.core.sync

import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.NOT_SYNCHRONIZED
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.PROCESSING
import io.openfuture.chain.network.component.NodeClock
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncBlockScheduler(
    private val nodeProperties: NodeProperties,
    private val syncStatus: SyncStatus,
    private val syncManager: SyncManager,
    private val networkApiService: NetworkApiService,
    private val nodeClock: NodeClock
) {

    @Scheduled(fixedRateString = "\${node.synchronization-interval}")
    fun syncBlock() {
        if (networkApiService.isChannelsEmpty()) {
            return
        }

        if (syncStatus.getSyncStatus() == NOT_SYNCHRONIZED || (syncStatus.getSyncStatus() == PROCESSING
                && isResponseTimeOut())) {
            syncManager.synchronize()
        }
    }

    private fun isResponseTimeOut(): Boolean =
        nodeClock.networkTime() - syncManager.getLastResponseTime() > nodeProperties.synchronizationResponseDelay!!

}