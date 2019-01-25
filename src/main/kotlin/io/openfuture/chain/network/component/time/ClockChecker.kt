package io.openfuture.chain.network.component.time

import io.openfuture.chain.network.component.time.ClockSyncStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.network.component.time.ClockSyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.net.ntp.NTPUDPClient
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
    private var status = SYNCHRONIZED


    @PostConstruct
    fun init() {
        check()
    }

    fun getStatus(): ClockSyncStatus = status

    private fun check() {
        try {
            val absOffsets = mutableListOf<Long>()
            for (address in properties.ntpServers) {
                try {
                    val ntpResponse = ntpClient.getTime(InetAddress.getByName(address))
                    ntpResponse.computeDetails()
                    absOffsets.add(ntpResponse.offset.absoluteValue)
                } catch (e: SocketTimeoutException) {
                    log.trace("Ntp server: $address is unavailable")
                } finally {
                    ntpClient.close()
                }
            }

            if (absOffsets.isEmpty()) {
                status = NOT_SYNCHRONIZED
                log.error("Clock is not synchronised: nobody answered")
                return
            }

            if (absOffsets.min()!! >= properties.ntpOffsetThreshold!!) {
                status = NOT_SYNCHRONIZED
                log.info("Clock is not synchronised: offset ${absOffsets.min()} is critical")
                return
            }

            status = SYNCHRONIZED
        } finally {
            executor.schedule({ check() }, status.checkDelay, TimeUnit.MILLISECONDS)
        }
    }

}