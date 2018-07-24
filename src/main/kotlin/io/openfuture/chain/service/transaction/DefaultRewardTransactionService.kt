package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.RewardTransactionEntityConverter
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.repository.RewardTransactionRepository
import io.openfuture.chain.service.RewardTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultRewardTransactionService(
    repository: RewardTransactionRepository,
    entityConverter: RewardTransactionEntityConverter
) : DefaultEmbeddedTransactionService<RewardTransaction, RewardTransactionData>(repository, entityConverter),
    RewardTransactionService {

    @Transactional
    override fun toBlock(tx: RewardTransaction, block: MainBlock): RewardTransaction {
        return super.baseToBlock(tx, block)
    }

    @Transactional
    override fun validate(dto: BaseTransactionDto<RewardTransactionData>) {
        super.baseValidate(dto)
    }

}