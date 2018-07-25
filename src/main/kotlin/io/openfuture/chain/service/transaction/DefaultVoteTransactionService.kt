package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.VoteTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.VoteTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: VoteTransactionEntityConverter,
    private val delegateService: DelegateService
) : DefaultTransactionService<VoteTransaction, VoteTransactionData>(repository,
    walletService, nodeClock, entityConverter), VoteTransactionService {

    @Transactional
    override fun addToBlock(tx: VoteTransaction, block: MainBlock): VoteTransaction {
        val delegate = delegateService.getByPublicKey(tx.delegateKey)
        walletService.changeWalletVote(tx.senderAddress, delegate, tx.getVoteType())
        return super.commonAddToBlock(tx, block)
    }

}