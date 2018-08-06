package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.vote.VoteTransactionPayload
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    uRepository: UVoteTransactionRepository,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val networkService: NetworkApiService
) : BaseTransactionService<VoteTransaction, UnconfirmedVoteTransaction>(repository, uRepository), VoteTransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction> {
        return unconfirmedRepository.findAllByOrderByFeeDesc()
    }

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction {
        val transaction = unconfirmedRepository.findOneByHash(message.hash)
        if (null != transaction) {
            return UnconfirmedVoteTransaction.of(message)
        }

        val savedUtx = super.save(UnconfirmedVoteTransaction.of(message))
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction {
        val savedUtx = super.save(UnconfirmedVoteTransaction.of(request))
        networkService.broadcast(VoteTransactionMessage(savedUtx))
        return savedUtx
    }

    @Transactional
    override fun synchronize(message: VoteTransactionMessage, block: MainBlock) {
        val persistTx = repository.findOneByHash(message.hash)
        if (null != persistTx) {
            return
        }

        val persistUtx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != persistUtx) {
            toBlock(persistUtx.hash, block)
            return
        }
        super.save(VoteTransaction.of(message))
    }

    override fun generateHash(request: VoteTransactionHashRequest): String {
        return TransactionUtils.generateHash(request.timestamp!!, request.fee!!, request.senderAddress!!,
            VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!))
    }

    @Transactional
    override fun toBlock(hash: String, block: MainBlock): VoteTransaction {
        val utx = getUnconfirmedByHash(hash)
        val type = utx.payload.getVoteType()
        updateWalletVotes(utx.payload.delegateKey, utx.senderAddress, type)
        return super.toBlock(utx, VoteTransaction.of(utx), block)
    }

    @Transactional
    override fun isValid(tx: VoteTransaction): Boolean {
        return isValidVoteCount(tx.senderAddress) && super.isValid(tx)
    }

    @Transactional
    override fun isValid(utx: UnconfirmedVoteTransaction): Boolean {
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
        val unconfirmedForVotes = unconfirmedRepository.findAll()
            .filter { it.senderAddress == senderAddress && it.payload.getVoteType() == VoteType.FOR }
            .count()

        if (consensusProperties.delegatesCount!! <= confirmedVotes + unconfirmedForVotes) {
            return false
        }
        return true
    }

}