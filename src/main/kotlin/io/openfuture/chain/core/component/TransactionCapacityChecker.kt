package io.openfuture.chain.core.component

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong


@Component
class TransactionCapacityChecker {

    private val counter = AtomicLong(0)
    private val capacity = AtomicLong(0)

    @Scheduled(fixedDelay = 10000)
    private fun schedule() {
        capacity.set(counter.getAndSet(0) / 10)
    }

    fun incrementCapacity() {
        counter.incrementAndGet()
    }

    fun getCapacity(): Long {
        return capacity.get()
    }

}