package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.state.ContractState
import io.openfuture.chain.core.repository.ContractStateRepository
import io.openfuture.chain.core.service.ContractStateService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.ContractStateMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultContractStateService(
    private val repository: ContractStateRepository,
    private val statePool: StatePool
) : BaseStateService<ContractState>(repository), ContractStateService {

    override fun updateStorage(address: String, storage: String): ContractStateMessage {
        val state = ContractStateMessage(address, storage)
        statePool.update(state)
        return state
    }

    @Transactional
    override fun commit(state: ContractState) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(state)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}