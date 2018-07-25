package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.unconfirmed.impl.UVoteTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.repository.UVoteTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.UVoteTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultUVoteTransactionService(
    repository: UVoteTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: UVoteTransactionEntityConverter,
    private val delegateService: DelegateService
) : DefaultUTransactionService<UVoteTransaction, VoteTransactionData>(repository,
    walletService, nodeClock, entityConverter), UVoteTransactionService {

    @Transactional
    override fun process(tx: UVoteTransaction) {
        val delegate = delegateService.getByPublicKey(tx.delegateKey)
        walletService.changeWalletVote(tx.senderAddress, delegate, tx.getVoteType())
    }

}