package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
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
    override fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction> = unconfirmedRepository.findAllByOrderByHeaderFeeDesc()

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction {
        if (isExists(message.hash)) {
            return UnconfirmedVoteTransaction.of(message)
        }

        val savedUtx = this.add(UnconfirmedVoteTransaction.of(message))
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction {
        val uTransaction = UnconfirmedVoteTransaction.of(request)
        if (isExists(uTransaction.hash)) {
            return uTransaction
        }

        val savedUtx = this.add(uTransaction)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    @Transactional
    override fun add(tx: VoteTransaction): VoteTransaction {
        val type = tx.payload.getVoteType()
        updateWalletVotes(tx.header.senderAddress, tx.payload.delegateKey, type)
        return super.add(tx)
    }

    @Transactional
    override fun synchronize(message: VoteTransactionMessage, block: MainBlock) {
        val tx = repository.findOneByHash(message.hash)
        if (null != tx) {
            return
        }

        val utx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != utx) {
            toBlock(utx, VoteTransaction.of(utx, block))
            return
        }
        this.add(VoteTransaction.of(message, block))
    }

    override fun generateHash(request: VoteTransactionHashRequest): String =
        TransactionUtils.generateHash(
            request.timestamp!!,
            request.fee!!,
            request.senderAddress!!,
            VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!)
        )

    @Transactional
    override fun toBlock(hash: String, block: MainBlock): VoteTransaction {
        val utx = getUnconfirmedByHash(hash)
        return toBlock(utx, VoteTransaction.of(utx, block))
    }

    @Transactional
    override fun isValid(tx: VoteTransaction): Boolean = isValidLocal(tx.header, tx.payload) && super.isValidBase(tx)

    @Transactional
    override fun isValid(utx: UnconfirmedVoteTransaction): Boolean = isValidLocal(utx.header, utx.payload) && super.isValidBase(utx)

    private fun isValidLocal(header: TransactionHeader, payload: VoteTransactionPayload): Boolean {
        return isExistsDelegate(payload.delegateKey) &&
            isValidVoteCount(header.senderAddress) &&
            !isAlreadyVote(header.senderAddress, payload.delegateKey) &&
            isExistsVoteType(payload.voteTypeId)
    }

    private fun updateWalletVotes(senderAddress: String, delegateKey: String, type: VoteType) {
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

    private fun isExistsDelegate(key: String): Boolean = delegateService.isExistsByPublicKey(key)

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = unconfirmedRepository.findAll()
            .filter { it.header.senderAddress == senderAddress && it.payload.getVoteType() == VoteType.FOR }
            .count()

        return consensusProperties.delegatesCount!! > confirmedVotes + unconfirmedForVotes
    }

    private fun isAlreadyVote(senderAddress: String, delegateKey: String): Boolean {
        val delegates = walletService.getVotesByAddress(senderAddress)
        return delegates.any { it.publicKey == delegateKey }
    }

    private fun isExistsVoteType(typeId: Int): Boolean {
        return VoteType.values().any { it.getId() == typeId }
    }

}