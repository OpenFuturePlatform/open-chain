package io.openfuture.chain.core.service.state

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.repository.DelegateStateRepository
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.DelegateStateMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateStateService(
    private val repository: DelegateStateRepository,
    private val consensusProperties: ConsensusProperties,
    private val statePool: StatePool
) : BaseStateService<DelegateState>(repository), DelegateStateService {

    companion object {
        const val DEFAULT_DELEGATE_RATING = 0L
    }


    override fun getAllDelegates(request: PageRequest): List<DelegateState> = repository.findLastAll(request)

    override fun getActiveDelegates(): List<DelegateState> =
        getAllDelegates(PageRequest(0, consensusProperties.delegatesCount!!)).sortedByDescending { it.rating }

    override fun updateRating(delegateKey: String, amount: Long): DelegateStateMessage {
        val delegateState = getCurrentState(delegateKey)
        val newDelegateState = DelegateStateMessage(delegateState.address, delegateState.rating + amount)
        statePool.update(newDelegateState)
        return newDelegateState
    }

    override fun addDelegate(delegateKey: String): DelegateStateMessage {
        val newDelegateState = DelegateStateMessage(delegateKey, DEFAULT_DELEGATE_RATING)
        statePool.update(newDelegateState)
        return newDelegateState
    }

    @Transactional
    override fun toBlock(message: DelegateStateMessage, block: MainBlock) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(DelegateState.of(message, block))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    private fun getCurrentState(address: String): DelegateStateMessage {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address) as? DelegateStateMessage
                ?: repository.findFirstByAddressOrderByBlockIdDesc(address)!!.toMessage()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}