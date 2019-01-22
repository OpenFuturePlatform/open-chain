package io.openfuture.chain.network.component.time

import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.SocketTimeoutException
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

    private var lastOffset = 0L
    private var nextQuizTime: Long? = null
    private var nearestNtpServer: InetAddress? = null
    private var quizResult = mutableMapOf<InetAddress, TimeInfo>()
    private val ntpClient = NTPUDPClient().apply { defaultTimeout = 3000 }
    private val ntpsInetAddress = properties.ntpServers.map { InetAddress.getByName(it) }.toList()


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
        if (nextQuizTime == null) {
            return true
        } else if (clock.currentTimeMillis() > nextQuizTime!!) {
            return true
        }
        return false
    }

    private fun isThreshold(offset: Long): Long {
        lastOffset = if (Math.abs(lastOffset) > Math.abs(offset) || Math.abs(offset) > properties.ntpOffsetThreshold!!) {
            offset
        } else {
            lastOffset
        }
        return lastOffset
    }

    private fun syncByNearestNtpServer(): Long {
        var info: TimeInfo?
        var tryQuiz: Boolean
        var attempt = 0
        val result = mutableListOf<TimeInfo>()
        do {
            try {
                info = ntpClient.getTime(nearestNtpServer)
                info.computeDetails()
                log.trace("Ntp server delay = ${info.delay} offset = ${info.offset}")
                result.add(info)
                tryQuiz = result.size != 3
            } catch (e: SocketTimeoutException) {
                tryQuiz = ++attempt != 6
            }
        } while (tryQuiz)

        val minOffset = result.minBy { it.offset }?.offset ?: return lastOffset
        return isThreshold(minOffset)
    }

    private fun startNtpQuiz(): Long {
        nextQuizTime = clock.currentTimeMillis().plus(properties.nextNtpServerInterval!!)
        quizResult.clear()

        for (address in ntpsInetAddress) {
            try {
                val info = ntpClient.getTime(address)
                info.computeDetails()
                log.trace("Ntp server ${address.hostName} delay = ${info.delay} offset = ${info.offset}")
                quizResult[address] = info
            } catch (e: SocketTimeoutException) {
                log.trace("Ntp server ${address.hostName} answers too long")
            }
        }

        val minDelay = quizResult.minBy { it.value.delay }!!
        nearestNtpServer = minDelay.key
        lastOffset = minDelay.value.offset

        return isThreshold(lastOffset)
    }

    private fun mitigate(offset: Long) {
        lock.writeLock().lock()
        try {
            clock.adjust(offset)

            syncRound.getAndIncrement()
            log.trace("CLOCK: Effective offset $offset")

            if (SYNCHRONIZED != status) {
                status = SYNCHRONIZED
            }

        } finally {
            lock.writeLock().unlock()
        }
    }
}