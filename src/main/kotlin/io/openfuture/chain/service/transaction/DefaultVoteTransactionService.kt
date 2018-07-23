package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.VoteTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.MainBlock
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
) : DefaultBaseTransactionService<VoteTransaction, VoteTransactionData>(repository,
    walletService, nodeClock, entityConverter), VoteTransactionService {

    @Transactional
    override fun toBlock(tx: VoteTransaction, block: MainBlock): VoteTransaction {
        val delegate = delegateService.getByPublicKey(tx.delegateKey)
        walletService.changeWalletVote(tx.senderAddress, delegate, tx.getVoteType())
        return super.commonToBlock(tx, block)
    }

    @Transactional
    override fun validate(dto: BaseTransactionDto<VoteTransactionData>) {
        this.defaultValidate(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    @Transactional
    override fun validate(request: BaseTransactionRequest<VoteTransactionData>) {
        this.defaultValidate(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}