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
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class ClockSynchronizer(
    private val clock: Clock,
    private val properties: NodeProperties,
    private val ntpClient: NTPUDPClient
) {

    companion object {
        private val log = LoggerFactory.getLogger(Clock::class.java)
    }


    private var lastOffset = 0L
    private var nextQuizTime: Long? = null
    private var nearestNtpServer: InetAddress? = null
    private val ntpsInetAddress = properties.ntpServers.map { InetAddress.getByName(it) }.toList()

    private val lock: ReadWriteLock = ReentrantReadWriteLock()

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
            if (isQuizTime()) {
                nextQuizTime = clock.currentTimeMillis().plus(properties.nextNtpServerInterval!!)

                val quizResult = startNtpQuiz(ntpsInetAddress)
                val minDelay = quizResult.minBy { it.value.delay }

                if (minDelay == null) {
                    status = NOT_SYNCHRONIZED
                    return
                }
                nearestNtpServer = minDelay.key
                val minOffset = minDelay.value.offset

                offset = if (getDeviation(lastOffset, quizResult.values.toList()) < 2 && Math.abs(minOffset) > properties.ntpOffsetThreshold!!) {
                    minOffset
                } else {
                    lastOffset
                }

            } else {
                val result = syncByNearestNtpServer(nearestNtpServer)
                val minOffset = result.minBy { Math.abs(it.offset) }?.offset

                if (minOffset == null) {
                    status = NOT_SYNCHRONIZED
                    return
                }

                if (getDeviation(lastOffset, result) < 2 && Math.abs(minOffset) > properties.ntpOffsetThreshold!!) {
                    lastOffset = minOffset
                }
                offset = lastOffset

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

    private fun isQuizTime(): Boolean = (nextQuizTime == null || clock.currentTimeMillis() > nextQuizTime!!)

    private fun getDeviation(lastOffset: Long, ntpResponses: List<TimeInfo>): Double {
        val mediana = (ntpResponses.sumBy { it.offset.toInt() } + lastOffset) / (ntpResponses.size + 1)
        var differencesMediana = 0.0
        ntpResponses.forEach { differencesMediana += Math.pow(((it.offset - mediana).toDouble()), 2.0) }
        return Math.sqrt(differencesMediana / ntpResponses.size)
    }

    private fun syncByNearestNtpServer(nearestNtpServer: InetAddress?): MutableList<TimeInfo> {
        var info: TimeInfo?
        var tryQuiz: Boolean
        var attempt = 0
        val result = mutableListOf<TimeInfo>()
        do {
            try {
                log.debug("Ask ${nearestNtpServer!!.hostName} server")
                info = ntpClient.getTime(nearestNtpServer)
                info.computeDetails()
                log.debug("Ntp server delay = ${info.delay} offset = ${info.offset}")
                result.add(info)
                tryQuiz = result.size < 3
            } catch (e: SocketTimeoutException) {
                tryQuiz = ++attempt < 6
                log.debug("Ntp server ${nearestNtpServer!!.hostName} answers too long")
            } finally {
                ntpClient.close()
            }
        } while (tryQuiz)
        return result
    }

    private fun startNtpQuiz(ntpsInetAddress: List<InetAddress>): MutableMap<InetAddress, TimeInfo> {
        val quizResult = mutableMapOf<InetAddress, TimeInfo>()
        for (address in ntpsInetAddress) {
            try {
                log.debug("Ask ${address.hostName} server")
                val info = ntpClient.getTime(address)
                info.computeDetails()
                log.debug("Ntp server ${address.hostName} delay = ${info.delay} offset = ${info.offset}")
                quizResult[address] = info
            } catch (e: SocketTimeoutException) {
                log.debug("Ntp server ${address.hostName} answers too long")
            } finally {
                ntpClient.close()
            }
        }
        return quizResult
    }

    private fun mitigate(offset: Long) {
        lock.writeLock().lock()
        try {
            clock.adjust(offset)

            log.debug("CLOCK: Effective offset $offset")

            if (SYNCHRONIZED != status) {
                status = SYNCHRONIZED
            }

        } finally {
            lock.writeLock().unlock()
        }
    }
}