//package io.openfuture.chain.core.sync
//
//import io.openfuture.chain.core.sync.SyncState.SyncStatusType.NOT_SYNCHRONIZED
//import org.springframework.stereotype.Component
//import kotlin.math.min
//
//@Component
//class SyncState {
//
//    @Volatile
//    private var chainStatus: SyncStatusType = NOT_SYNCHRONIZED
//
//    @Volatile
//    private var clockStatus: SyncStatusType = NOT_SYNCHRONIZED
//
//
//    @Synchronized
//    fun getChainStatus(): SyncStatusType = chainStatus
//
//    @Synchronized
//    fun getClockStatus(): SyncStatusType = clockStatus
//
//    @Synchronized
//    fun getNodeStatus(): SyncStatusType = SyncStatusType.values()
//        .first { min(chainStatus.priority, clockStatus.priority) == it.priority }
//
//    @Synchronized
//    fun setChainStatus(status: SyncStatusType) {
//        chainStatus = status
//    }
//
//    @Synchronized
//    fun setClockStatus(status: SyncStatusType) {
//        clockStatus = status
//    }
//
//    enum class SyncStatusType(val priority: Int) {
//        SYNCHRONIZED(3),
//        PROCESSING(2),
//        NOT_SYNCHRONIZED(1)
//    }
//
//}
