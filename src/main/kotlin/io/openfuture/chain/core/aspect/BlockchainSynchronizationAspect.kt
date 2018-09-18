package io.openfuture.chain.core.aspect

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.SYNCHRONIZED
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component


@Aspect
@Component
class BlockchainSynchronizationAspect(
    private val syncStatus: SyncStatus
) {

    @Before("@annotation(annotation)")
    fun annotated(annotation: BlockchainSynchronized) {
        if (syncStatus.getSyncStatus() != SYNCHRONIZED) {
            throw SynchronizationException("Application is not synchronized! Current sync status: ${syncStatus.getSyncStatus()}!")
        }
    }

}