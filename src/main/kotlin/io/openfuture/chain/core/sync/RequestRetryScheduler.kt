package io.openfuture.chain.core.sync

import io.openfuture.chain.network.property.NodeProperties
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
class RequestRetryScheduler(
    private val properties: NodeProperties
) {

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


    fun startRequestScheduler(future: ScheduledFuture<*>?, action: Runnable): ScheduledFuture<*>? {
        return if (future == null || future.isDone) {
            executor.scheduleWithFixedDelay(
                action,
                properties.syncExpiry!!,
                properties.syncExpiry!!,
                TimeUnit.MILLISECONDS)
        } else {
            future
        }
    }

}