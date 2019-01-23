package io.openfuture.chain.network.component.time

import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.NtpV3Packet
import org.apache.commons.net.ntp.TimeInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.MathContext
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.annotation.PostConstruct

@Component
class ClockSynchronizer(
    private val clock: Clock,
    private val properties: NodeProperties,
    private val ntpClient: NTPUDPClient
) {

    companion object {
        private val log = LoggerFactory.getLogger(Clock::class.java)
    }


    private val percentThreshold = 5.0f
    private val ntpInetAddresses = properties.ntpServers.map { InetAddress.getByName(it) }
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    private var lastOffset = 0L
    private var ntpSynced: Boolean = true
    private var nextQuizTime: Long? = null
    private var nearestNtpServer: InetAddress? = null
    private var nextSyncTime: Long = 0

    @Volatile
    private var status: SyncStatus = NOT_SYNCHRONIZED

    @PostConstruct
    fun init() {
        executor.schedule({ sync() }, nextSyncTime, TimeUnit.MILLISECONDS)
    }

    private fun sync() {
        lock.writeLock().lock()
        try {
            if (SYNCHRONIZED != status) {
                status = PROCESSING
            }
            log.debug("Clock synchronization in PROCESSING")
            if (nextQuizTime == null || clock.currentTimeMillis() > nextQuizTime!!) {
                nextQuizTime = clock.currentTimeMillis().plus(properties.nextNtpServerInterval!!)

                val quizResult = startNtpQuiz(ntpInetAddresses)
                val nearestNtp = quizResult.minBy { it.value.delay }

                if (nearestNtp == null) {
                    setNotSync()
                    return
                }

                nearestNtpServer = nearestNtp.key
                lastOffset = if (getDeviation(lastOffset, quizResult.values.toList()) <= percentThreshold
                    && Math.abs(nearestNtp.value.offset) < properties.ntpOffsetThreshold!!) {
                    nearestNtp.value.offset
                } else {
                    getOffset(nearestNtpServer) ?: return
                }
            } else {
                lastOffset = getOffset(nearestNtpServer) ?: return
            }
            mitigate(lastOffset)
        } finally {
            executor.schedule({ sync() }, nextSyncTime, TimeUnit.MILLISECONDS)
            lock.writeLock().unlock()
        }
    }

    fun isSyncByNtp(): Boolean = this.ntpSynced

    fun getStatus(): SyncStatus {
        lock.readLock().lock()
        try {
            return status
        } finally {
            lock.readLock().unlock()
        }
    }

    private fun setNotSync() {
        nextQuizTime = null
        status = NOT_SYNCHRONIZED
        nextSyncTime = properties.timeFailedSyncInterval!!
    }

    private fun getPrecision(rawPrecision: Int): Float =
        BigDecimal(2).pow(rawPrecision, MathContext(3))
            .multiply(BigDecimal(1000), MathContext(4)).toFloat()

    private fun getDeviation(lastOffset: Long, ntpResponses: List<TimeInfo>): Float {
        val square = 2.0
        val median = (ntpResponses.sumBy { it.offset.toInt() } + lastOffset) / (ntpResponses.size + 1)
        var sumDiff = 0.0
        ntpResponses.forEach { sumDiff += Math.pow(((it.offset - median).toDouble()), square) }
        val dev = Math.sqrt(sumDiff / ntpResponses.size).toFloat()
        log.debug("Deviation = $dev")
        return dev
    }

    private fun getOffset(nearestNtpServer: InetAddress?): Long? {
        val result = syncByNearestNtpServer(nearestNtpServer)
        if (result.size < 2) {
            setNotSync()
            return null
        }
        val minOffset = result.minBy { Math.abs(it.offset) }!!.offset

        if (getDeviation(lastOffset, result) > percentThreshold) {
            setNotSync()
            if (Math.abs(minOffset) > properties.ntpOffsetThreshold!!) {
                ntpSynced = false
            }
            return null
        }
        ntpSynced = true
        return minOffset
    }

    private fun syncByNearestNtpServer(nearestNtpServer: InetAddress?): MutableList<TimeInfo> {
        val requiredResponses = 6
        val maxAttempts = 6
        val result = mutableListOf<TimeInfo>()

        var info: TimeInfo?
        var tryQuiz: Boolean
        var attempt = 0
        var message: NtpV3Packet
        log.debug("Start send request to ${nearestNtpServer!!.hostName}")
        do {
            try {
                info = ntpClient.getTime(nearestNtpServer)
                Thread.sleep(1000)
                info.computeDetails()
                message = info.message
                log.debug("Ntp stratum = ${message.stratum}, precision = ${getPrecision(message.precision)} ms, delay = ${info.delay}, offset = ${info.offset} ")
                result.add(info)
                tryQuiz = result.size < requiredResponses
            } catch (e: SocketTimeoutException) {
                tryQuiz = ++attempt < maxAttempts
                log.debug("Ntp server ${nearestNtpServer.hostName} answers too long")
            } finally {
                ntpClient.close()
            }
        } while (tryQuiz)
        return result
    }

    private fun startNtpQuiz(ntpInetAddresses: List<InetAddress>): MutableMap<InetAddress, TimeInfo> {
        val quizResult = mutableMapOf<InetAddress, TimeInfo>()

        var info: TimeInfo
        var message: NtpV3Packet
        for (address in ntpInetAddresses) {
            try {
                info = ntpClient.getTime(address)
                info.computeDetails()
                message = info.message
                log.debug("Ntp stratum = ${message.stratum}, precision = ${getPrecision(message.precision)} ms, delay = ${info.delay}, offset = ${info.offset} ")
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
            nextSyncTime = properties.timeSyncInterval!!
        } finally {
            lock.writeLock().unlock()
        }
    }
}