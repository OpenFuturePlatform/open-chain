package io.openfuture.chain.util

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class NodeTime(
        private val adjustment : AtomicLong = AtomicLong(0)
) {

    fun now() : Long{
        return System.currentTimeMillis() + adjustment.toLong()
    }

    fun addAdjustment(adjustment: Long) : Long{
        return this.adjustment.addAndGet(adjustment)
    }
}