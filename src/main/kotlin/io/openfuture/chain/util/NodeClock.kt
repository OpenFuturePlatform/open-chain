package io.openfuture.chain.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Component
class NodeClock {

    companion object {
        private val log = LoggerFactory.getLogger(NodeClock::class.java)
    }

    @Volatile
    private var adjustment : Long = 0

    private val networkTimeOffsets: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    private val lock: ReentrantLock = ReentrantLock()

    fun nodeTime() : Long = System.currentTimeMillis()

    fun networkTime() : Long {
        return nodeTime() + adjustment
    }

    fun addTimeOffset(remoteAddress: String, offset: Long) {
        networkTimeOffsets[remoteAddress] = offset
        recalculateAdjustment()
    }

    fun removeTimeOffset(remoteAddress: String) {
        networkTimeOffsets.remove(remoteAddress)
        recalculateAdjustment()
    }

    private fun recalculateAdjustment() {
        lock.lock()
        if (networkTimeOffsets.size % 2 == 1 && networkTimeOffsets.size > 2) {
            val offsetList = ArrayList(networkTimeOffsets.values)
            offsetList.sort()
            adjustment = offsetList[networkTimeOffsets.size / 2]
            log.info("Time adjustment was changed: $adjustment")
        }
        lock.unlock()
    }
}