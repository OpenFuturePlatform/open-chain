package io.openfuture.chain.network.service

import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.domain.NetworkBlockRequest
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantReadWriteLock

@Service
class SyncService(
    private val networkApiService: NetworkApiService,
    private val blockService: BlockService,
    private val lock: ReentrantReadWriteLock
) {
    private val lastHash: String? = null

    fun sync() {
        try {
            lock.writeLock().lock()
            networkApiService.send(NetworkBlockRequest(blockService.getLast().hash))
        } catch (e: Exception) {
            lock.writeLock().unlock()
            sync()
        }
    }

    fun isLastSyncBlock(hash: String) {
        if (hash == lastHash) {
            lock.writeLock().unlock()
        }
    }

}
