package io.openfuture.chain.core.component

import io.openfuture.chain.network.component.node.NodeClock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong


@Component
class BlockCapacityChecker(
    nodeClock: NodeClock
) {
    private val lastBlockTime = AtomicLong(nodeClock.networkTime())
    private val capacity = AtomicLong(0)
    private val counter = AtomicLong(0)
    private val avg = AtomicLong(0)


    @Scheduled(fixedDelay = 10000)
    private fun schedule() {
        if (counter.get() == 0L) {
            avg.set(0)
            return
        }
        avg.set(capacity.getAndSet(0) / counter.getAndSet(0))
    }

    fun incrementCapacity(time: Long) {
        val diff = time - lastBlockTime.getAndSet(time)
        capacity.getAndAdd(diff)
        counter.getAndIncrement()
    }

    fun getCapacity(): Long {
        return avg.get()
    }

}