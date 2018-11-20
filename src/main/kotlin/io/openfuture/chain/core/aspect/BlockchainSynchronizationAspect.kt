package io.openfuture.chain.core.aspect

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.core.sync.SyncState
import io.openfuture.chain.core.sync.SyncState.SyncStatusType.SYNCHRONIZED
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component


@Aspect
@Component
class BlockchainSynchronizationAspect(
    private val syncStatus: SyncState
) {

    @Before("@annotation(annotation)")
    fun annotated(annotation: BlockchainSynchronized) {
        if (syncStatus.getChainStatus() != SYNCHRONIZED) {
            throw SynchronizationException("Application is not synchronized! Current sync status: ${syncStatus.getChainStatus()}!")
        }
    }

}