package io.openfuture.chain.network.sync.impl

import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SyncBlockHandler
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.PROCESSING
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncBlockScheduler(
    private val nodeProperties: NodeProperties,
    private val syncBlockHandler: SyncBlockHandler,
    private val syncManager: SyncManager,
    private val networkApiService: NetworkApiService
) {

    @Scheduled(fixedRateString = "\${node.synchronization-interval}")
    fun syncBlock() {
        if (networkApiService.isChannelsEmpty()) {
            return
        }

        if (syncManager.getSyncStatus() == NOT_SYNCHRONIZED || (syncManager.getSyncStatus() == PROCESSING && isResponseTimeOut())) {
            syncBlockHandler.synchronize()
        }
    }

    private fun isResponseTimeOut() = System.currentTimeMillis() - syncBlockHandler.getLastResponseTime() > nodeProperties.synchronizationResponseDelay!!

}