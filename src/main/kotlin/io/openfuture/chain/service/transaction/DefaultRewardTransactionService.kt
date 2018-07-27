package io.openfuture.chain.service.transaction

import io.openfuture.chain.domain.transaction.RewardTransactionDto
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.repository.RewardTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.RewardTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// todo need to add to common transactions services
@Service
class DefaultRewardTransactionService(
    private val repository: RewardTransactionRepository,
    private val baseService: BaseTransactionService,
    private val walletService: WalletService
) : RewardTransactionService {

    @Transactional
    override fun toBlock(dto: RewardTransactionDto, block: MainBlock) {
        if (baseService.isExists(dto.hash)) {
            return
        }

        val tx = dto.toEntity()
        tx.block = block
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
        repository.save(tx)
    }

    @Transactional
    override fun toBlock(tx: RewardTransaction, block: MainBlock) {
        if (baseService.isExists(tx.hash)) {
            return
        }

        tx.block = block
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
        repository.save(tx)
    }

}
