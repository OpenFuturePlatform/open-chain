package io.openfuture.chain.network.component.time

import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.*
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
    private var status: SyncStatus = NOT_SYNCHRONIZED
    private var syncRound: AtomicInteger = AtomicInteger()
    private var deviation: AtomicLong = AtomicLong()


    @Scheduled(fixedDelayString = "\${node.time-synchronization-interval}")
    fun sync() {
        lock.writeLock().lock()
        try {
            if (SYNCHRONIZED != status) {
                status = PROCESSING
            }
            offsets.clear()

            val addresses = addressHolder.getRandomList(selectionSize).asSequence().map { it.address }.toSet()
            connectionService.sendTimeSyncRequest(addresses)
        } finally {
            lock.writeLock().unlock()

            Thread.sleep(properties.syncResponseDelay!!)
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
                deviation.getAndSet(max(deviation.get(), offset))
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

    fun getStatus(): SyncStatus = status

    private fun mitigate() {
        if ((selectionSize * 2 / 3) > offsets.size) {
            status = NOT_SYNCHRONIZED
            return
        }

        clock.adjust(getEffectiveOffset())
        syncRound.getAndIncrement()
        log.info("Effective offset ${getEffectiveOffset()}")

        if (SYNCHRONIZED != status) {
            status = SYNCHRONIZED
        }
    }

    private fun getEffectiveOffset(): Long = Math.round(offsets.average())

    private fun getRemoteOffset(msg: ResponseTimeMessage, destinationTime: Long): Long =
        ((msg.receiveTime.minus(msg.originalTime)).plus(msg.transmitTime.minus(destinationTime))).div(2)

    private fun getScale(): Double = 1.div(Math.log(Math.E.plus(syncRound.get())))

    private fun isExpired(msg: ResponseTimeMessage, destinationTime: Long): Boolean =
        properties.syncResponseDelay!! < Math.abs(destinationTime.minus(msg.originalTime))

    private fun isOutOfBound(offset: Long): Boolean = (deviation.get() * getScale()) < Math.abs(offset)

}