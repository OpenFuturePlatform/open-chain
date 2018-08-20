package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.*
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import org.slf4j.LoggerFactory
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

    companion object {
        val log = LoggerFactory.getLogger(DefaultVoteTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction> = unconfirmedRepository.findAllByOrderByHeaderFeeDesc()

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction {
        val utx = UnconfirmedVoteTransaction.of(message)

        if (isExists(utx.hash)) {
            return utx
        }

        validate(utx)
        val savedUtx = this.save(utx)
        networkService.broadcast(message)
        return savedUtx
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction {
        val utx = UnconfirmedVoteTransaction.of(request)
        validate(utx)

        if (isExists(utx.hash)) {
            return utx
        }

        val savedUtx = this.save(utx)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    @Transactional
    override fun toBlock(message: VoteTransactionMessage, block: MainBlock): VoteTransaction {
        val tx = repository.findOneByHash(message.hash)
        if (null != tx) {
            return tx
        }

        val utx = unconfirmedRepository.findOneByHash(message.hash)
        if (null != utx) {
            return confirm(utx, VoteTransaction.of(utx, block))
        }

        return this.save(VoteTransaction.of(message, block))
    }

    @Transactional
    override fun isValid(message: VoteTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedVoteTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    override fun save(tx: VoteTransaction): VoteTransaction {
        val type = tx.payload.getVoteType()
        updateWalletVotes(tx.header.senderAddress, tx.payload.delegateKey, type)
        return super.save(tx)
    }

    private fun validate(utx: UnconfirmedVoteTransaction) {
        if (!isExistsDelegate(utx.payload.delegateKey)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }

        if (!isValidVoteCount(utx.header.senderAddress)) {
            throw ValidationException("Incorrect votes count", INCORRECT_VOTES_COUNT)
        }

        if (!isAlreadyVote(utx.header.senderAddress, utx.payload.delegateKey)) {
            throw ValidationException("Address: ${utx.header.senderAddress} already vote for delegate with key: ${utx.payload.delegateKey}")
        }

        if (!isExistsVoteType(utx.payload.voteTypeId)) {
            throw ValidationException("Vote type with id: ${utx.payload.voteTypeId} is not exists")
        }

        if (!isValidFee(utx.header.senderAddress, utx.header.fee)) {
            throw ValidationException("Insufficient balance", INSUFFICIENT_BALANCE)
        }

        super.validateBase(utx)
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

    private fun isValidFee(senderAddress: String, fee: Long): Boolean {
        val balance = walletService.getBalanceByAddress(senderAddress)
        val unspentBalance = balance - baseService.getAllUnconfirmedByAddress(senderAddress).map { it.header.fee }.sum()
        return fee in 0..unspentBalance
    }

}