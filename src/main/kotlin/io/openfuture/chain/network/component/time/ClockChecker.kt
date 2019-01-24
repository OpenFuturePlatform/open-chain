package io.openfuture.chain.network.component.time

import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import kotlin.math.absoluteValue

@Component
class ClockChecker(
    private val properties: NodeProperties,
    private val ntpClient: NTPUDPClient
) {

    companion object {
        private val log = LoggerFactory.getLogger(ClockChecker::class.java)
    }

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val ntpInetAddresses = properties.ntpServers.map { InetAddress.getByName(it) }
    private var status = SYNCHRONIZED
    private var offset: Long = 0L


    @PostConstruct
    fun sync() {
        try {
            val quizResult = startNtpQuiz(ntpInetAddresses)
            val list = mutableListOf<Long>()
            list.addAll(quizResult.map { it.offset })

            val minOffset = list.minBy { it.absoluteValue }!!
            if (minOffset >= properties.ntpOffsetThreshold!!) {
                status = NOT_SYNCHRONIZED
                offset = minOffset
                return
            }
            status = SYNCHRONIZED
        } finally {
            executor.schedule({ sync() }, properties.checkTimeInterval!!, TimeUnit.MILLISECONDS)
        }
    }

    fun getOffset(): Long = offset

    fun getStatus(): SyncStatus = status

    private fun startNtpQuiz(ntpInetAddresses: List<InetAddress>): MutableList<TimeInfo> {
        val quizResult = mutableListOf<TimeInfo>()
        var info: TimeInfo
        do {
            for (address in ntpInetAddresses) {
                try {
                    info = ntpClient.getTime(address)
                    info.computeDetails()
                    log.trace("Ntp server ${address.hostName} answer ${info.offset}")

                    quizResult.add(info)
                } catch (e: SocketTimeoutException) {
                    log.trace("Ntp server ${address.hostName} answers too long")
                } finally {
                    ntpClient.close()
                }
            }
        } while (quizResult.size < 1)
        return quizResult
    }

}