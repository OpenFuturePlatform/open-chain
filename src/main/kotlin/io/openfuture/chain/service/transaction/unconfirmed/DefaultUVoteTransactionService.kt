package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.impl.UVoteTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.UVoteTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.UVoteTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
class DefaultUVoteTransactionService(
    repository: UVoteTransactionRepository,
    entityConverter: UVoteTransactionEntityConverter,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties
) : DefaultManualUTransactionService<UVoteTransaction, VoteTransactionData>(repository, entityConverter),
    UVoteTransactionService {

    @Transactional
    override fun validate(request: BaseTransactionRequest<VoteTransactionData>) {
        if (!isValidVoteCount(request.data!!.senderAddress)) {
            throw ValidationException("Wallet ${request.data!!.senderAddress} already spent all votes!")
        }

        super.validate(request)
    }

    @Transactional
    override fun validate(dto: BaseTransactionDto<VoteTransactionData>) {
        if (!isValidVoteCount(dto.data.senderAddress)) {
            throw ValidationException("Wallet ${dto.data.senderAddress} already spent all votes!")
        }

        super.validate(dto)
    }

    @Transactional
    override fun process(tx: UVoteTransaction) {
        updateWalletVotes(tx)
    }

    private fun updateWalletVotes(tx: UVoteTransaction) {
        val delegate = delegateService.getByPublicKey(tx.delegateKey)
        val wallet = walletService.getByAddress(tx.senderAddress)

        when (tx.getVoteType()) {
            VoteType.FOR -> wallet.votes.add(delegate)
            VoteType.AGAINST -> wallet.votes.remove(delegate)
        }

        walletService.save(wallet)
    }

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = getAll()
            .filter { it.senderAddress == senderAddress && it.getVoteType() == VoteType.FOR }
            .count()

        val unspentVotes = confirmedVotes + unconfirmedForVotes
        if (consensusProperties.delegatesCount!! <= unspentVotes) {
            return false
        }
        return true
    }

}