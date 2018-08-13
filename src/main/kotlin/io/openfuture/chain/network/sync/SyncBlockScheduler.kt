package io.openfuture.chain.network.sync

import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SynchronizationStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.network.sync.SynchronizationStatus.PROCESSING
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncBlockScheduler(
    private val nodeProperties: NodeProperties,
    private val syncBlockHandler: SyncBlockHandler,
    private val networkApiService: NetworkApiService
) {

    @Scheduled(fixedRateString = "\${node.synchronization-interval}")
    fun syncBlock() {
        if (networkApiService.isChannelsEmpty()) {
            return
        }

        if (syncBlockHandler.getSyncStatus() == NOT_SYNCHRONIZED || (syncBlockHandler.getSyncStatus() == PROCESSING && isResponseTimeOut())) {
            syncBlockHandler.synchronize()
        }
    }

    private fun isResponseTimeOut() = System.currentTimeMillis() - syncBlockHandler.getLastResponseTime() > nodeProperties.synchronizationResponseDelay!!

}