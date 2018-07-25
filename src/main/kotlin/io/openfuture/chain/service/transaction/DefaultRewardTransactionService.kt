package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.URewardTransaction
import io.openfuture.chain.repository.RewardTransactionRepository
import io.openfuture.chain.service.RewardTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultRewardTransactionService(
    repository: RewardTransactionRepository
) : DefaultTransactionService<RewardTransaction, URewardTransaction>(repository),
    RewardTransactionService {

    override fun add(uTx: URewardTransaction): RewardTransaction {
        TODO("not implemented")
    }

    @Transactional
    override fun toBlock(tx: RewardTransaction, block: MainBlock): RewardTransaction {
        return baseToBlock(tx, block)
    }

}