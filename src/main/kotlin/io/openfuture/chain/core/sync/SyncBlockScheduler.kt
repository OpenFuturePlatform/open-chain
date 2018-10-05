package io.openfuture.chain.core.sync

import io.openfuture.chain.core.sync.SyncState.SyncStatusType.NOT_SYNCHRONIZED
import io.openfuture.chain.core.sync.SyncState.SyncStatusType.PROCESSING
import io.openfuture.chain.network.component.Clock
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncBlockScheduler(
    private val nodeProperties: NodeProperties,
    private val syncState: SyncState,
    private val syncManager: SyncManager,
    private val networkApiService: NetworkApiService,
    private val clock: Clock
) {

    @Scheduled(fixedRateString = "\${node.synchronization-interval}")
    fun syncBlock() {
        if (networkApiService.isChannelsEmpty()) {
            return
        }

        if (syncState.getChainStatus() == NOT_SYNCHRONIZED || (syncState.getChainStatus() == PROCESSING
                && isResponseTimeOut())) {
            syncManager.synchronize()
        }
    }

    private fun isResponseTimeOut(): Boolean =
        clock.currentTimeMillis() - syncManager.getLastResponseTime() > nodeProperties.synchronizationResponseDelay!!

}