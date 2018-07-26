package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.URewardTransaction
import io.openfuture.chain.repository.RewardTransactionRepository
import io.openfuture.chain.repository.URewardTransactionRepository
import io.openfuture.chain.service.RewardTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultRewardTransactionService(
    repository: RewardTransactionRepository,
    uRepository: URewardTransactionRepository
) : DefaultTransactionService<RewardTransaction, URewardTransaction>(repository, uRepository),
    RewardTransactionService