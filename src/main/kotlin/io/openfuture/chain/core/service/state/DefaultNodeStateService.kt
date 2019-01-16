package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.state.NodeState
import io.openfuture.chain.core.model.entity.state.payload.NodePayload
import io.openfuture.chain.core.repository.NodeStateRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.NodeStateService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultNodeStateService(
    private val repository: NodeStateRepository,
    private val statePool: StatePool,
    private val blockService: BlockService
) : BaseStateService<NodeState>(repository), NodeStateService {

    override fun updateOwnVotesByNodeId(nodeId: String, address: String, type: VoteType) {
        val state = getCurrentState(nodeId)

        val ownVotes = state.payload.data.ownVotes
        when (type) {
            VoteType.FOR -> ownVotes.add(address)
            VoteType.AGAINST -> ownVotes.remove(address)
        }

        val newState = createState(
            nodeId,
            NodePayload(NodePayload.Data(state.payload.data.address, state.payload.data.registrationDate, ownVotes))
        )

        statePool.update(newState)
    }

    override fun addDelegate(nodeId: String, address: String, timestamp: Long) {
        val state = createState(nodeId, NodePayload(NodePayload.Data(address, timestamp)))
        statePool.update(state)
    }

    private fun createState(nodeId: String, payload: NodePayload): NodeState =
        NodeState(nodeId, blockService.getLast().height + 1, payload)

    private fun getCurrentState(nodeId: String): NodeState {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(nodeId) as? NodeState
                ?: repository.findLastByAddress(nodeId)!!
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}