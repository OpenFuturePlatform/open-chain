package io.openfuture.chain.block

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.property.ConsensusProperties
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock


@Component
class TimeSlot(
    private val properties: ConsensusProperties,
    private val clock: NodeClock
) {

    private var epochTime: Long = 0L
    private lateinit var producer: Delegate

    private val lock = ReentrantReadWriteLock()


    fun getEpochTime(): Long = getSynchronized(epochTime)

    fun getProducer(): Delegate = getSynchronized(producer)

    fun setRoundStartTime(epochTime: Long) {
        try {
            lock.writeLock().lock()
            this.epochTime = epochTime
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun setProducer(producer: Delegate) {
        try {
            lock.writeLock().lock()
            this.producer = producer
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun setEpochTime(epochTime: Long) {
        this.epochTime = epochTime
    }

    fun getCurrentSlotNumber() : Long = getSlotNumber(clock.networkTime())

    fun getSlotNumber(time: Long): Long = ((time - epochTime) / properties.timeSlotDuration!!)

    fun verifyTimeSlot(currentTime: Long, block: Block)
        = (getSlotNumber(currentTime) == getSlotNumber(block.timestamp))

    private fun <T> getSynchronized(value: T): T {
        try {
            lock.readLock().lock()
            return value
        } finally {
            lock.readLock().unlock()
        }
    }

}