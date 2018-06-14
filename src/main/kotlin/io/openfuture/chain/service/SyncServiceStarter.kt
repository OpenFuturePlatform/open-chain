package io.openfuture.chain.service

import io.openfuture.chain.property.NodeProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class SyncServiceStarter(
        private val timeSyncService: TimeSyncService,
        private val properties: NodeProperties
        ) : ApplicationListener<ApplicationReadyEvent> {

    private val serverExecutor = Executors.newSingleThreadScheduledExecutor()

    override fun onApplicationEvent(event: ApplicationReadyEvent?) {
        serverExecutor.scheduleAtFixedRate(
                timeSyncService, 0, properties.timeSyncInterval!!, TimeUnit.SECONDS)
    }
}