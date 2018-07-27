package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.impl.URewardTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.URewardTransaction
import io.openfuture.chain.repository.URewardTransactionRepository
import io.openfuture.chain.service.URewardTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultURewardTransactionService(
    repository: URewardTransactionRepository,
    entityConverter: URewardTransactionEntityConverter
) : DefaultEmbeddedUTransactionService<URewardTransaction, RewardTransactionData>(repository, entityConverter),
    URewardTransactionService