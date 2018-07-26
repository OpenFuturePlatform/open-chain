package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.RewardTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.repository.RewardTransactionRepository
import io.openfuture.chain.service.RewardTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultRewardTransactionService(
    repository: RewardTransactionRepository,
    entityConverter: RewardTransactionEntityConverter
) : DefaultEmbeddedTransactionService<RewardTransaction, RewardTransactionData>(repository, entityConverter),
    RewardTransactionService