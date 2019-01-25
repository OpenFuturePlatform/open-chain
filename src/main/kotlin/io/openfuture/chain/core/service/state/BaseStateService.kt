package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.model.entity.block.Block
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
            return repository.findFirstByAddressOrderByBlockIdDesc(address)
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

    override fun getByAddressAndBlock(address: String, block: Block): T? {
        BlockchainLock.readLock.lock()
        try {
            return repository.findFirstByAddressAndBlockHeightLessThanEqualOrderByBlockHeightDesc(address, block.height)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun deleteBlockStates(blockHeights: List<Long>) {
        BlockchainLock.writeLock.lock()
        try {
            repository.deleteAllByBlockHeightIn(blockHeights)
            repository.flush()
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}