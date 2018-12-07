package io.openfuture.chain.network.component.time

import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.net.ntp.NTPUDPClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class ClockSynchronizer(
        private val clock: Clock,
        private val properties: NodeProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(Clock::class.java)
    }

    private var lastQuizTime: Long? = null
    private var nearestNtpServer: InetAddress? = null
    private val ntpClient = NTPUDPClient()
    private val ntpsInetAddress = properties.ntpServers.map { InetAddress.getByName(it) }.toList()
    private var quizResult = mutableMapOf<InetAddress, NtpResult>()


    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    private val syncRound: AtomicInteger = AtomicInteger()

    @Volatile
    private var status: SyncStatus = NOT_SYNCHRONIZED

    @Scheduled(fixedDelayString = "\${node.time-sync-interval}")
    fun sync() {
        lock.writeLock().lock()
        var offset = 0L
        try {
            if (SYNCHRONIZED != status) {
                status = PROCESSING
            }
            offset = if (isQuizTime()) {
                startNtpQuiz()
            } else {
                syncByNearestNtpServer()
            }

        } finally {
            lock.writeLock().unlock()

            mitigate(offset)
        }
    }


    fun getStatus(): SyncStatus {
        lock.readLock().lock()
        try {
            return status
        } finally {
            lock.readLock().unlock()
        }
    }

    private fun isQuizTime(): Boolean {
        if (lastQuizTime == null) {
            return true
        } else if (clock.currentTimeMillis() > lastQuizTime!!.plus(properties.nextNtpServerInterval!!)) {
            return true
        }
        return false
    }

    private fun syncByNearestNtpServer(): Long {
        val info = ntpClient.getTime(nearestNtpServer)
        info.computeDetails()
        return info.offset
    }

    private fun startNtpQuiz(): Long {
        lastQuizTime = clock.currentTimeMillis()
        quizResult.clear()
        for (address in ntpsInetAddress) {
            val info = ntpClient.getTime(address)
            info.computeDetails()
            quizResult[address] = NtpResult(info.delay, info.offset)
        }

        val minDelay = quizResult.minBy { it.value.delay }!!
        nearestNtpServer = minDelay.key

        return minDelay.value.offset
    }

    private fun mitigate(offset: Long) {
        lock.writeLock().lock()
        try {
            clock.adjust(offset)

            syncRound.getAndIncrement()
            log.info("CLOCK: Effective offset $offset")

            if (SYNCHRONIZED != status) {
                status = SYNCHRONIZED
            }

        } finally {
            lock.writeLock().unlock()
        }

    }


//    private fun getEffectiveOffset(): Long = Math.round(offsets.average())
//
//    private fun getRemoteOffset(msg: ResponseTimeMessage, destinationTime: Long): Long =
//            ((msg.receiveTime.minus(msg.originalTime)).plus(msg.transmitTime.minus(destinationTime))).div(2)
//
//    private fun getScale(): Double = 1.div(Math.log(Math.E.plus(syncRound.get())))
//
//    private fun isExpired(msg: ResponseTimeMessage, destinationTime: Long): Boolean =
//            properties.expiry!! < Math.abs(destinationTime.minus(msg.originalTime))
//
//    private fun isOutOfBound(offset: Long): Boolean = (deviation.get() * getScale()) < Math.abs(offset)
}