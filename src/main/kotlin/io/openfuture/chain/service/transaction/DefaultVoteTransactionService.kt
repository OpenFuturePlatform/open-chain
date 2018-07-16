package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.delegate.DelegateInfo
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.Stakeholder
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    private val nodeClock: NodeClock,
    private val delegateService: DelegateService,
    private val stakeholderService: StakeholderService,
    private val consensusProperties: ConsensusProperties
) : DefaultBaseTransactionService<VoteTransaction>(repository), VoteTransactionService {

    @Transactional
    override fun add(dto: VoteTransactionDto) {
        //todo need to add validation
        //todo need to think about calculate the vote weight
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return
        }

        saveAndBroadcast(VoteTransaction.of(dto))
    }

    @Transactional
    override fun add(request: VoteTransactionRequest) {
        saveAndBroadcast(VoteTransaction.of(nodeClock.networkTime(), request))
    }

    private fun saveAndBroadcast(tx: VoteTransaction) {
        updateDelegateRatingByVote(tx.getVoteType(), tx.senderKey, DelegateInfo(tx.delegateHost, tx.delegatePort))
        repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

    private fun updateDelegateRatingByVote(voteType: VoteType, stakeholderKey: String, delegateInfo: DelegateInfo) {
        val stakeholder = stakeholderService.getByPublicKey(stakeholderKey)
        val delegate = delegateService.getByHostAndPort(delegateInfo.networkAddress.host, delegateInfo.networkAddress.port)

        check(stakeholder, delegate, voteType)

        if (voteType == VoteType.FOR) {
            delegate.rating += 1
            stakeholder.votes.add(delegate)
        } else {
            delegate.rating -= 1
            stakeholder.votes.remove(delegate)
        }

        stakeholderService.save(stakeholder)
        delegateService.save(delegate)
    }

    private fun check(stakeholder: Stakeholder, delegate: Delegate, voteType: VoteType) {
        if (consensusProperties.delegatesCount!! <= stakeholder.votes.size && voteType == VoteType.FOR) {
            throw IllegalStateException("Bad vote transaction")
        }

        if (stakeholder.votes.contains(delegate) && voteType == VoteType.FOR) {
            throw IllegalStateException("Bad vote transaction")
        }

        if (!stakeholder.votes.contains(delegate) && voteType == VoteType.AGAINST) {
            throw IllegalStateException("Bad vote transaction")
        }
    }

}