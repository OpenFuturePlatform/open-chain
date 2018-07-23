package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.VoteTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.property.ConsensusProperties
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
    private val consensusProperties: ConsensusProperties,
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
        voteCountValidate(dto.data.senderAddress)
        this.defaultValidate(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    @Transactional
    override fun validate(request: BaseTransactionRequest<VoteTransactionData>) {
        voteCountValidate(request.data!!.senderAddress)
        this.defaultValidate(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

    private fun voteCountValidate(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val pendingTransactions = getAllPending()
        val unconfirmedForVotes = pendingTransactions
            .filter { it.senderAddress == senderAddress && it.getVoteType() == VoteType.FOR }
            .count()

        val unspentVotes = confirmedVotes + unconfirmedForVotes
        if (consensusProperties.delegatesCount!! <= unspentVotes) {
            return false
        }
        return true
    }

}