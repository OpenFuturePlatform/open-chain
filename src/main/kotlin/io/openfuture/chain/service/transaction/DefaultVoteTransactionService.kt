package io.openfuture.chain.service.transaction

import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.repository.UVoteTransactionRepository
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.VoteTransactionService
import io.openfuture.chain.util.DictionaryUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    uRepository: UVoteTransactionRepository,
    private val delegateService: DelegateService
) : DefaultTransactionService<VoteTransaction, UVoteTransaction, VoteTransactionData, VoteTransactionDto>(repository, uRepository),
    VoteTransactionService {


    @Transactional
    override fun toBlock(dto: VoteTransactionDto, block: MainBlock) {
        val type = DictionaryUtils.valueOf(VoteType::class.java, dto.data.voteTypeId)
        updateWalletVotes(dto.data.delegateKey, dto.data.senderAddress, type)
        super.toBlock(dto, block)
    }

    @Transactional
    override fun toBlock(tx: VoteTransaction, block: MainBlock) {
        updateWalletVotes(tx.delegateKey, tx.senderAddress, tx.getVoteType())
        super.toBlock(tx, block)
    }

    private fun updateWalletVotes(delegateKey: String, senderAddress: String, type: VoteType) {
        val delegate = delegateService.getByPublicKey(delegateKey)
        val wallet = walletService.getByAddress(senderAddress)

        when (type) {
            VoteType.FOR -> {
                wallet.votes.add(delegate)
            }
            VoteType.AGAINST -> {
                wallet.votes.remove(delegate)
            }
        }
        walletService.save(wallet)
    }

}