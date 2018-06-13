package io.openfuture.chain.service

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class SyncServiceStarter(
        private val timeSyncService: TimeSyncService
) : ApplicationListener<ApplicationReadyEvent> {

    private val serverExecutor = Executors.newSingleThreadExecutor()

    override fun onApplicationEvent(event: ApplicationReadyEvent?) {
        serverExecutor.execute(timeSyncService)
    }
}