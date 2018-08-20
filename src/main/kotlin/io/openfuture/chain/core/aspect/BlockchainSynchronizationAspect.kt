package io.openfuture.chain.core.aspect

import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Aspect
@Component
class BlockchainSynchronizationAspect(
    private val syncManager: SyncManager
) {

    companion object {
        val log = LoggerFactory.getLogger(BlockchainSynchronizationAspect::class.java)
    }

    @Around("@annotation(io.openfuture.chain.core.annotation.BlockchainSynchronized)")
    fun annotated(joinPoint: ProceedingJoinPoint): Any? {
        if (syncManager.getSyncStatus() != SynchronizationStatus.SYNCHRONIZED) {
           log.error("Application is not synchronized!")
            return null
        }
        return joinPoint.proceed()
    }

}