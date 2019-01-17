package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.repository.StateRepository
import io.openfuture.chain.core.service.StateService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class BaseStateService<T : State>(
    private val repository: StateRepository<T>
) : StateService<T> {

    override fun getLastByAddress(address: String): T? {
        BlockchainLock.readLock.lock()
        try {
            return repository.findLastByAddress(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getByAddress(address: String): List<T> {
        BlockchainLock.readLock.lock()
        try {
            return repository.findByAddress(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getByAddressAndBlockId(address: String, blockId: Long): T? {
        BlockchainLock.readLock.lock()
        try {
            return repository.findByAddressAndBlockId(address, blockId)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional
    override fun create(state: T): T = repository.save(state)

}