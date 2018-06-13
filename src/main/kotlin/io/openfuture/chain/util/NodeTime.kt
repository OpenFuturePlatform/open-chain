package io.openfuture.chain.util

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class NodeTime(
        private var adjustment : AtomicLong = AtomicLong(0)
) {

    fun now() : Long{
        return System.currentTimeMillis() + adjustment.toLong()
    }

    fun setAdjustment(adjustment: Long){
        this.adjustment.addAndGet(adjustment)
    }
}