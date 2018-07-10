package io.openfuture.chain.service

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
        repository: VoteTransactionRepository,
        private val nodeClock: NodeClock,
        private val stakeholderService: StakeholderService
) : BaseTransactionService<VoteTransaction>(repository), VoteTransactionService {

    // --  votes logic // todo need to create TransactionVoteService with extends this
    @Transactional
    override fun addVote(dto: VoteTransactionDto): VoteTransaction {
        //todo need to add validation
        val vote = VoteDto(dto.senderKey, dto.delegateKey, dto.voteType, dto.weight) //todo need to think about calculate the vote weight
        stakeholderService.updateDelegateRatingByVote(vote)
        return repository.save(dto.toEntity())
    }

    override fun createVote(data: VoteTransactionData): VoteTransactionDto {
        val networkTime = nodeClock.networkTime()
        val hash = TransactionUtils.calculateHash(networkTime, data)
        return VoteTransactionDto(networkTime, data.amount, data.recipientKey, data.senderKey, data.senderSignature,
                hash, data.voteType, data.delegateKey, data.weight)
    }

}