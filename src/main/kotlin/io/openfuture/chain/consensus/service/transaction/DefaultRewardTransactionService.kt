package io.openfuture.chain.consensus.service.transaction

import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.model.entity.transaction.RewardTransaction
import io.openfuture.chain.consensus.repository.RewardTransactionRepository
import io.openfuture.chain.consensus.service.RewardTransactionService
import io.openfuture.chain.consensus.service.WalletService
import io.openfuture.chain.core.service.CommonTransactionService
import io.openfuture.chain.consensus.model.dto.transaction.RewardTransactionDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// todo need to add to common transactions services
@Service
class DefaultRewardTransactionService(
    private val repository: RewardTransactionRepository,
    private val commonService: CommonTransactionService,
    private val walletService: WalletService
) : RewardTransactionService {

    @Transactional
    override fun toBlock(dto: RewardTransactionDto, block: MainBlock) {
        if (commonService.isExists(dto.hash)) {
            return
        }

        val tx = dto.toEntity()
        tx.block = block
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
        repository.save(tx)
    }

    @Transactional
    override fun toBlock(tx: RewardTransaction, block: MainBlock) {
        if (commonService.isExists(tx.hash)) {
            return
        }

        tx.block = block
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
        repository.save(tx)
    }

}
