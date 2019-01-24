package io.openfuture.chain.network.component.time

import io.openfuture.chain.network.component.time.exception.ClockException
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.SocketTimeoutException
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

    private val ntpInetAddresses = properties.ntpServers.map { InetAddress.getByName(it) }


    @PostConstruct
    private fun sync() {
        try {
            val quizResult = startNtpQuiz(ntpInetAddresses)
            val list = mutableListOf<Long>()
            list.addAll(quizResult.map { it.offset })

            if (list.minBy { it.absoluteValue }!! >= properties.ntpOffsetThreshold!!) {
                log.error("Time is not synchronized by ntp servers")
                throw ClockException("Please set up Time synchronization by the ntp servers, cause: Current time offset from ntp servers is ${list.min()!!} ms.")
            }
        } catch (ex: ClockException) {
            log.error(ex.message!!)
            System.exit(1)
        }
    }

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