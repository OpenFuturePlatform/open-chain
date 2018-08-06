package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.service.NetworkService
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
internal class DefaultVoteTransactionService(
    repository: TransactionRepository<VoteTransaction>,
    uRepository: UTransactionRepository<UVoteTransaction>,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val networkService: NetworkService
) : BaseTransactionService<VoteTransaction, UVoteTransaction>(repository, uRepository), VoteTransactionService {

    @Transactional
    override fun add(message: VoteTransactionMessage): UVoteTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UVoteTransaction.of(message)
        }

        val utx = UVoteTransaction.of(message)
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = super.add(utx)
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: VoteTransactionRequest): UVoteTransaction {
        val utx = UVoteTransaction.of(request)
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = super.add(utx)
        networkService.broadcast(VoteTransactionMessage(savedUtx))
        return savedUtx
    }

    override fun generateHash(request: VoteTransactionHashRequest): String {
        return TransactionUtils.generateHash(request.timestamp!!, request.fee!!,
            VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!))
    }

    @Transactional
    override fun toBlock(utx: UVoteTransaction, block: MainBlock): VoteTransaction {
        val type = utx.payload.getVoteType()
        updateWalletVotes(utx.payload.delegateKey, utx.senderAddress, type)
        return super.toBlock(utx, VoteTransaction.of(utx), block)
    }

    private fun isValid(utx: UVoteTransaction): Boolean {
        return isValidVoteCount(utx.senderAddress) && super.isValid(utx)
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

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = uRepository.findAll()
            .filter { it.senderAddress == senderAddress && it.payload.getVoteType() == VoteType.FOR }
            .count()

        if (consensusProperties.delegatesCount!! <= confirmedVotes + unconfirmedForVotes) {
            return false
        }
        return true
    }

}