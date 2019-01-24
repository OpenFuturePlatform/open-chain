package io.openfuture.chain.core.service.state

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.repository.DelegateStateRepository
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.DelegateStateMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Sort
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
        private const val DEFAULT_DELEGATE_RATING = 0L
    }


    override fun getAllDelegates(request: PageRequest): List<DelegateState> = repository.findLastAll(request)

    override fun getActiveDelegates(): List<DelegateState> {
        val sortBy = setOf("rating", "id")
        return getAllDelegates(PageRequest(0, consensusProperties.delegatesCount!!, sortBy, Sort.Direction.DESC))
    }

    override fun isExistsByPublicKey(key: String): Boolean = null != getLastByAddress(key)

    override fun isExistsByPublicKeys(publicKeys: List<String>): Boolean = publicKeys.all { isExistsByPublicKey(it) }

    override fun updateRating(delegateKey: String, amount: Long): DelegateStateMessage {
        val state = getCurrentState(delegateKey)
        val newState = DelegateStateMessage(state.address, state.rating + amount, state.walletAddress, state.createDate)
        statePool.update(newState)
        return newState
    }

    override fun addDelegate(delegateKey: String, walletAddress: String, createDate: Long): DelegateStateMessage {
        val newState = DelegateStateMessage(delegateKey, DEFAULT_DELEGATE_RATING, walletAddress, createDate)
        statePool.update(newState)
        return newState
    }

    @Transactional
    override fun commit(state: DelegateState) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(state)
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