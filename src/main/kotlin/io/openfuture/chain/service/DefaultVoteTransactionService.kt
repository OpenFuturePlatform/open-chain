package io.openfuture.chain.service

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.vote.VoteTransactionData
import io.openfuture.chain.entity.VoteTransaction
import io.openfuture.chain.repository.VoteTransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
        private val nodeClock: NodeClock,
        private val repository: VoteTransactionRepository
) : BaseTransactionService<VoteTransaction>(repository), VoteTransactionService {

    @Transactional
    override fun add(dto: VoteTransactionDto): VoteTransaction {
        //todo need to add validation
        return repository.save(VoteTransaction.of(dto))
    }

    override fun create(data: VoteTransactionData): VoteTransactionDto {
        val networkTime = nodeClock.networkTime()
        val hash = HashUtils.generateHash(data.getByteData(networkTime))
        return VoteTransactionDto(networkTime, hash, data)
    }

}