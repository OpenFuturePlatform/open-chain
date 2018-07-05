package io.openfuture.chain.service

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.vote.VoteTransactionData
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Vote
import io.openfuture.chain.entity.VoteTransaction
import io.openfuture.chain.repository.VoteRepository
import io.openfuture.chain.repository.VoteTransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
        private val nodeClock: NodeClock,
        private val repository: VoteTransactionRepository,
        private val voteRepository: VoteRepository
) : BaseTransactionService<VoteTransaction>(repository), VoteTransactionService {

    @Transactional
    override fun add(dto: VoteTransactionDto): VoteTransaction {
        //todo need to add validation

        val persistTransaction = repository.save(VoteTransaction.of(dto))
        val votes = dto.data.votes.map { voteRepository.save(Vote.of(persistTransaction, it)) }
        persistTransaction.votes.addAll(votes)
        return persistTransaction
    }

    override fun create(data: VoteTransactionData): VoteTransactionDto {
        val networkTime = nodeClock.networkTime()
        val hash = HashUtils.generateHash(data.getByteData(networkTime))
        return VoteTransactionDto(networkTime, hash, data)
    }

}