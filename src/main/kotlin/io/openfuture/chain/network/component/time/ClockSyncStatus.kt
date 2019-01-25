package io.openfuture.chain.network.component.time

enum class ClockSyncStatus (val checkDelay: Long) {
    SYNCHRONIZED(3600000),
    NOT_SYNCHRONIZED(60000)
}