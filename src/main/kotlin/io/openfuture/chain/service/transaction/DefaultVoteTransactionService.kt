package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.VoteTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository
) : DefaultTransactionService<VoteTransaction, UVoteTransaction>(repository),
    VoteTransactionService {

    override fun add(uTx: UVoteTransaction): VoteTransaction {
        TODO("not implemented")
    }

    @Transactional
    override fun toBlock(tx: VoteTransaction, block: MainBlock): VoteTransaction {
        return baseToBlock(tx, block)
    }

}