package io.openfuture.chain.core.service.state

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.repository.DelegateStateRepository
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.WalletStateService
import io.openfuture.chain.core.service.WalletVoteService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.DelegateStateMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateStateService(
    private val repository: DelegateStateRepository,
    private val walletVoteService: WalletVoteService,
    private val walletStateService: WalletStateService,
    private val consensusProperties: ConsensusProperties,
    private val statePool: StatePool
) : BaseStateService<DelegateState>(repository), DelegateStateService {

    companion object {
        const val DEFAULT_DELEGATE_RATING = 0L
    }


    override fun getAllDelegates(): List<DelegateState> = repository.findLastAll()

    override fun getActiveDelegates(): List<DelegateState> =
        getAllDelegates().sortedBy { it.rating }.take(consensusProperties.delegatesCount!!)

    //todo change
    override fun updateRating(publicKey: String) {
        val rating = walletVoteService.getVotesForDelegate(publicKey).map {
            walletStateService.getBalanceByAddress(it.id.address)
        }.sum()

        statePool.update(DelegateStateMessage(publicKey, rating))
    }

    override fun addDelegate(publicKey: String) {
        statePool.update(DelegateStateMessage(publicKey, DEFAULT_DELEGATE_RATING))
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

}