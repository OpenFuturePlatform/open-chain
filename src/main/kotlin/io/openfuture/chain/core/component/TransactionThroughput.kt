package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong


@Component
class TransactionThroughput(
    private val consensusProperties: ConsensusProperties
) {

    private val counter = AtomicLong(0)
    private val height = AtomicLong(1)


    fun updateThroughput(transactionsCount: Int, currentHeight: Long) {
        counter.addAndGet(transactionsCount.toLong())
        height.set(currentHeight)
    }

    fun getThroughput(): Long = counter.get() / (consensusProperties.getPeriod() / 1000 * height.get())

}