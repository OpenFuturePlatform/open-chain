package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.payload.DelegatePayload
import io.openfuture.chain.core.repository.DelegateStateRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateStateService(
    private val repository: DelegateStateRepository,
    private val statePool: StatePool,
    private val blockService: BlockService
) : BaseStateService<DelegateState>(repository), DelegateStateService {

    override fun getOwnVotesByNodeId(nodeId: String): List<String> {
        BlockchainLock.readLock.lock()
        try {
            return getLastByAddress(nodeId)?.payload?.data?.ownVotes ?: emptyList()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun updateOwnVoteByNodeId(nodeId: String, address: String, type: VoteType) {
        val state = getCurrentState(nodeId)

        val ownVotes = state.payload.data.ownVotes
        when (type) {
            VoteType.FOR -> ownVotes.add(address)
            VoteType.AGAINST -> ownVotes.remove(address)
        }

        val newState = createState(
            nodeId,
            DelegatePayload(DelegatePayload.Data(state.payload.data.walletAddress, state.payload.data.registrationDate, ownVotes))
        )

        statePool.update(newState)
    }

    override fun getAllDelegates(): List<DelegateState> = repository.findLastAll()

    override fun addDelegate(nodeId: String, address: String, timestamp: Long) {
        val state = createState(nodeId, DelegatePayload(NodePayload.Data(address, timestamp)))
        statePool.update(state)
    }

    private fun createState(nodeId: String, payload: DelegatePayload): DelegateState =
        DelegateState(nodeId, blockService.getLast().height + 1, payload)

    private fun getCurrentState(nodeId: String): DelegateState {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(nodeId) as? DelegateState
                ?: repository.findLastByAddress(nodeId)!!
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}