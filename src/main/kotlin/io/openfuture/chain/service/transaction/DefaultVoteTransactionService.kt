package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.domain.vote.VoteDto
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.VoteTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    private val nodeClock: NodeClock,
    private val delegateService: DelegateService
) : DefaultBaseTransactionService<VoteTransaction>(repository), VoteTransactionService {

    @Transactional
    override fun add(dto: VoteTransactionDto): VoteTransaction {
        //todo need to add validation
        //todo need to think about calculate the vote weight
        val vote = VoteDto(dto.voteType, dto.senderKey, dto.delegateInfo)
        delegateService.updateDelegateRatingByVote(vote)
        return repository.save(VoteTransaction.of(dto))
    }

    override fun create(data: VoteTransactionData): VoteTransactionDto = VoteTransactionDto.of(nodeClock.networkTime(), data)

}