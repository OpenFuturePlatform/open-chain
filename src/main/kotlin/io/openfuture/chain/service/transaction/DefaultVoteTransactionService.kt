package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.VoteTransactionEntityConverter
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
import io.openfuture.chain.util.DictionaryUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    entityConverter: VoteTransactionEntityConverter,
    private val consensusProperties: ConsensusProperties,
    private val delegateService: DelegateService
) : DefaultManualTransactionService<VoteTransaction, VoteTransactionData>(repository, entityConverter),
    VoteTransactionService {


    @Transactional
    override fun toBlock(dto: BaseTransactionDto<VoteTransactionData>, block: MainBlock) {
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

    @Transactional
    override fun validate(dto: BaseTransactionDto<VoteTransactionData>) {
        if (!isValidVoteCount(dto.data.senderAddress)) {
            throw ValidationException("Wallet ${dto.data.senderAddress} already spent all votes!")
        }
        super.validate(dto)
    }

    @Transactional
    override fun validate(request: BaseTransactionRequest<VoteTransactionData>) {
        if (!isValidVoteCount(request.data!!.senderAddress)) {
            throw ValidationException("Wallet ${request.data!!.senderAddress} already spent all votes!")
        }
        super.validate(request)
    }

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = getAllPending()
            .filter { it.senderAddress == senderAddress && it.getVoteType() == VoteType.FOR }
            .count()

        val unspentVotes = confirmedVotes + unconfirmedForVotes
        if (consensusProperties.delegatesCount!! <= unspentVotes) {
            return false
        }
        return true
    }

}