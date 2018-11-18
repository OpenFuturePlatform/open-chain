package io.openfuture.chain.network.component.time

import io.openfuture.chain.core.sync.SyncState
import io.openfuture.chain.core.sync.SyncState.SyncStatusType.*
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.max

@Component
class ClockSynchronizer(
    private val clock: Clock,
    private val syncState: SyncState,
    private val properties: NodeProperties,
    private val connectionService: ConnectionService,
    private val addressHolder: ExplorerAddressesHolder
) {

    companion object {
        private val log = LoggerFactory.getLogger(Clock::class.java)
    }

    @Volatile private var offsets: MutableList<Long> = mutableListOf()

    private val selectionSize: Int = properties.getRootAddresses().size
    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    private var syncRound: AtomicInteger = AtomicInteger()
    private var deviation: AtomicLong = AtomicLong()


    @Scheduled(fixedDelayString = "\${node.time-synchronization-interval}")
    fun sync() {
        lock.writeLock().lock()
        val addresses = addressHolder.getRandomList(selectionSize).asSequence().map { it.address }.toSet()
        try {
            if (SYNCHRONIZED != syncState.getClockStatus()) {
                syncState.setClockStatus(PROCESSING)
            }
            offsets.clear()
            connectionService.sendTimeSyncRequest(addresses)
        } finally {
            lock.writeLock().unlock()

            waitResponseMessages(addresses.size, properties.synchronizationResponseDelay!!)

            mitigate()
        }
    }

    fun add(msg: ResponseTimeMessage, destinationTime: Long) {
        lock.writeLock().lock()
        try {
            if (isExpired(msg, destinationTime)) {
                return
            }

            val offset = getRemoteOffset(msg, destinationTime)

            if (0 == syncRound.get()) {
                deviation.getAndSet(max(deviation.get(), Math.abs(offset)))
                offsets.add(offset)
                return
            }

            if (!isOutOfBound(offset)) {
                offsets.add(offset)
            }

        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun waitResponseMessages(expectedNumberResponses: Int, maxWaitTime: Long) {
        if (expectedNumberResponses != 0) {
            var countWaitingSteps = 0
            val second = 1000L
            val maxCountDelays = maxWaitTime / second

            while (offsets.size != expectedNumberResponses && countWaitingSteps < maxCountDelays) {
                ++countWaitingSteps
                Thread.sleep(second)
            }
        }
    }

    private fun mitigate() {
        if (offsets.size < (selectionSize * 2 / 3)) {
            syncState.setClockStatus(NOT_SYNCHRONIZED)
            return
        }

        clock.adjust(getEffectiveOffset())
        syncRound.getAndIncrement()
        log.info("Effective offset ${getEffectiveOffset()}")

        if (SYNCHRONIZED != syncState.getClockStatus()) {
            syncState.setClockStatus(SYNCHRONIZED)
        }
    }

    private fun getEffectiveOffset(): Long = Math.round(offsets.average())

    private fun getRemoteOffset(msg: ResponseTimeMessage, destinationTime: Long): Long =
        ((msg.receiveTime.minus(msg.originalTime)).plus(msg.transmitTime.minus(destinationTime))).div(2)

    private fun getScale(): Double = 1.div(Math.log(Math.E.plus(syncRound.get())))

    private fun isExpired(msg: ResponseTimeMessage, destinationTime: Long): Boolean =
        properties.expiry!! < Math.abs(destinationTime.minus(msg.originalTime))

    private fun isOutOfBound(offset: Long): Boolean = (deviation.get() * getScale()) < Math.abs(offset)

}