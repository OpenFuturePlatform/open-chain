package io.openfuture.chain.network.component

import io.openfuture.chain.core.sync.SyncState
import io.openfuture.chain.core.sync.SyncState.SyncStatusType.PROCESSING
import io.openfuture.chain.core.sync.SyncState.SyncStatusType.SYNCHRONIZED
import io.openfuture.chain.network.message.network.TimeMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Clock (
    private val syncState: SyncState,
    private val nodeProperties: NodeProperties,
    private val connectionService: ConnectionService
) {

    @Volatile private var offset: Long = 0
    @Volatile private var offsets: MutableList<Long> = mutableListOf()
    @Volatile private var delay: Long = 0


    @Scheduled(fixedDelay = 5000)
    fun sync() {
        val addresses = nodeProperties.getRootAddresses()
        syncState.setClockStatus(PROCESSING)
        val message = TimeMessage(isSynchronized(), currentTimeMillis())
        addresses.forEach { connectionService.connectAndSend(it, message) }
    }

    @Synchronized
    fun currentTimeMillis(): Long {
        return System.currentTimeMillis() + offset
    }

    @Synchronized
    fun adjust(msg: TimeMessage) {
        offsets.add(calculateOffset(msg))
        delay = calculateDelay(msg)

        if (0 < offsets.size) {
            offset = offsets.asSequence().groupBy { it }.maxBy { it.value.size }!!.key
            syncState.setClockStatus(SYNCHRONIZED)
        }
    }

    fun isSynchronized(): Boolean = SYNCHRONIZED == syncState.getClockStatus()

    private fun calculateOffset(msg: TimeMessage): Long =
        ((msg.receiveTime - msg.originalTime) + (msg.transmitTime - msg.destinationTime)) / 2

    private fun calculateDelay(msg: TimeMessage): Long =
        (msg.destinationTime - msg.originalTime) - (msg.receiveTime - msg.transmitTime)

}