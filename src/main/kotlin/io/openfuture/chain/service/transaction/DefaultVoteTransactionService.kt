package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.VoteTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.main
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: VoteTransactionEntityConverter,
    private val delegateService: DelegateService
) : DefaultBaseTransactionService<VoteTransaction, VoteTransactionDto, VoteTransactionRequest>(repository,
    walletService, nodeClock, entityConverter), VoteTransactionService {

    @Transactional
    override fun addToBlock(tx: VoteTransaction, block: MainBlock): VoteTransaction {
        val delegate = delegateService.getByPublicKey(tx.delegateKey)
        walletService.changeWalletVote(tx.senderAddress, delegate, tx.getVoteType())
        return super.commonAddToBlock(tx, block)
    }

}