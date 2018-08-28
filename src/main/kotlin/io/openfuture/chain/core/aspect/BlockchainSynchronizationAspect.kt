package io.openfuture.chain.core.aspect

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component


@Aspect
@Component
class BlockchainSynchronizationAspect(
    private val syncManager: SyncManager
) {

    @Before("@annotation(annotation)")
    fun annotated(annotation: BlockchainSynchronized) {
        if (syncManager.getSyncStatus() != SynchronizationStatus.SYNCHRONIZED) {
            throw SynchronizationException("Application is not synchronized! Current sync status: ${syncManager.getSyncStatus()}!")
        }
    }

}