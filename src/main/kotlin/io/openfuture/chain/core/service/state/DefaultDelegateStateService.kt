package io.openfuture.chain.core.service.state

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.repository.DelegateStateRepository
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.WalletStateService
import io.openfuture.chain.core.service.WalletVoteService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.DelegateStateMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
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
        getAllDelegates().sortedByDescending { it.rating }.take(consensusProperties.delegatesCount!!)

    //todo change
    override fun updateRating(message: VoteTransactionMessage) {
        val persistVotes = walletVoteService.getVotesForDelegate(message.delegateKey).map { it.id.address }.toMutableList()
        when (VoteType.getById(message.voteTypeId)) {
            VoteType.FOR -> persistVotes.add(message.senderAddress)
            VoteType.AGAINST -> persistVotes.remove(message.senderAddress)
        }

        val rating = persistVotes.map { walletStateService.getBalanceByAddress(it) }.sum()
        statePool.update(DelegateStateMessage(message.delegateKey, rating))
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