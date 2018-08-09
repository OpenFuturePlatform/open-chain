package io.openfuture.chain.core.sync

import io.openfuture.chain.network.service.NetworkInnerService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SyncBlockScheduler(
    private val syncBlockHandler: SyncBlockHandler,
    private val networkInnerService: NetworkInnerService
) {

    @Scheduled(fixedDelayString = "10000")
    fun syncBlock() {
        if (!syncBlockHandler.isSynchronize() && !networkInnerService.getChannels().isEmpty()) {
            syncBlockHandler.synchronize()
        }
    }


}