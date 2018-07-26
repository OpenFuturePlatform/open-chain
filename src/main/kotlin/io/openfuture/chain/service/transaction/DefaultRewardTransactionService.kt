package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.URewardTransaction
import io.openfuture.chain.repository.RewardTransactionRepository
import io.openfuture.chain.repository.URewardTransactionRepository
import io.openfuture.chain.service.RewardTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultRewardTransactionService(
    repository: RewardTransactionRepository,
    uRepository: URewardTransactionRepository
) : DefaultTransactionService<RewardTransaction, URewardTransaction>(repository, uRepository),
    RewardTransactionService {

    @Transactional
    override fun toBlock(tx: RewardTransaction, block: MainBlock): RewardTransaction {
        tx.block = block
        return repository.save(tx)
    }
}