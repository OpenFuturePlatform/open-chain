package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.RewardTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.repository.RewardTransactionRepository
import io.openfuture.chain.service.RewardTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultRewardTransactionService(
    repository: RewardTransactionRepository,
    walletService: WalletService,
    entityConverter: RewardTransactionEntityConverter
) : DefaultEmbeddedTransactionService<RewardTransaction, RewardTransactionData>(repository, walletService,
    entityConverter), RewardTransactionService {

    @Transactional
    override fun addToBlock(tx: RewardTransaction, block: MainBlock): RewardTransaction {
        return this.commonAddToBlock(tx, block)
    }

}