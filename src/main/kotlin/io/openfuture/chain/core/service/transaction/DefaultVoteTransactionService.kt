package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType
import io.openfuture.chain.core.exception.model.ExceptionType.*
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.core.service.WalletVoteService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    uRepository: UVoteTransactionRepository,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val walletVoteService: WalletVoteService
) : ExternalTransactionService<VoteTransaction, UnconfirmedVoteTransaction>(repository, uRepository), VoteTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultVoteTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getUnconfirmedCount(): Long = unconfirmedRepository.count()

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): VoteTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedVoteTransaction> =
        unconfirmedRepository.findAllByOrderByHeaderFeeDesc(request)

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction = unconfirmedRepository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getUnconfirmedBySenderAgainstDelegate(senderAddress: String, nodeId: String): UnconfirmedVoteTransaction? =
        (unconfirmedRepository as UVoteTransactionRepository)
            .findOneByHeaderSenderAddressAndPayloadNodeIdAndPayloadVoteTypeId(senderAddress, nodeId, VoteType.AGAINST.getId())


    @Transactional(readOnly = true)
    override fun getLastVoteForDelegate(senderAddress: String, nodeId: String): VoteTransaction =
        (repository as VoteTransactionRepository)
            .findFirstByHeaderSenderAddressAndPayloadNodeIdAndPayloadVoteTypeIdOrderByHeaderTimestampDesc(senderAddress, nodeId, VoteType.FOR.getId())
            ?: throw NotFoundException("Last vote for delegate transaction not found")

    @BlockchainSynchronized
    @Transactional
    override fun add(message: VoteTransactionMessage) {
        BlockchainLock.writeLock.lock()
        try {
            super.add(UnconfirmedVoteTransaction.of(message))
        } catch (ex: CoreException) {
            log.debug(ex.message)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction {
        BlockchainLock.writeLock.lock()
        try {
            return super.add(UnconfirmedVoteTransaction.of(request))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @Transactional
    override fun toBlock(message: VoteTransactionMessage, block: MainBlock): VoteTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val tx = repository.findOneByFooterHash(message.hash)
            if (null != tx) {
                return tx
            }

            val utx = unconfirmedRepository.findOneByFooterHash(message.hash)
            if (null != utx) {
                return confirm(utx, VoteTransaction.of(utx, block))
            }

            return this.save(VoteTransaction.of(message, block))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun updateState(message: VoteTransactionMessage) {
        walletStateService.updateBalanceByAddress(message.senderAddress, -message.fee)
    }

    override fun verify(message: VoteTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedVoteTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: VoteTransaction): VoteTransaction {
        walletVoteService.updateVoteByAddress(tx.header.senderAddress, tx.payload.nodeId, tx.payload.getVoteType())
        return super.save(tx)
    }

    override fun validate(utx: UnconfirmedVoteTransaction) {
        super.validate(utx)

        if (!isExistsVoteType(utx.payload.voteTypeId)) {
            throw ValidationException("Vote type with id: ${utx.payload.voteTypeId} is not exists")
        }

        if (!isValidFee(utx.payload.voteTypeId, utx.header.fee)) {
            throw ValidationException("Incorrect fee")
        }
    }

    @Transactional(readOnly = true)
    override fun validateNew(utx: UnconfirmedVoteTransaction) {
        if (!isValidActualBalance(utx.header.senderAddress, utx.header.fee)) {
            throw ValidationException("Insufficient actual balance", ExceptionType.INSUFFICIENT_ACTUAL_BALANCE)
        }

        if (!isExistsDelegate(utx.payload.nodeId)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }

        if (isAlreadySentVote(utx.header.senderAddress, utx.payload.nodeId, utx.payload.voteTypeId)) {
            throw ValidationException("Address ${utx.header.senderAddress} has already sent vote for delegate ${utx.payload.nodeId}",
                ALREADY_SENT_VOTE)
        }

        if (VoteType.FOR.getId() == utx.payload.voteTypeId) {
            if (isAlreadyVoted(utx.header.senderAddress, utx.payload.nodeId)) {
                throw ValidationException("Address ${utx.header.senderAddress} has already voted for delegate ${utx.payload.nodeId}",
                    ALREADY_VOTED_FOR_DELEGATE)
            }

            if (!isVoteLeft(utx.header.senderAddress)) {
                throw ValidationException("No votes left", INCORRECT_VOTES_COUNT)
            }
        }
    }

    private fun isExistsDelegate(nodeId: String): Boolean = delegateService.isExistsByNodeId(nodeId)

    private fun isVoteLeft(senderAddress: String): Boolean {
        val confirmedVotes = walletVoteService.getVotesByAddress(senderAddress).size
        val unconfirmedForVotes = unconfirmedRepository.findAllByHeaderSenderAddress(senderAddress).asSequence()
            .filter { VoteType.FOR == it.payload.getVoteType() }
            .count()

        return consensusProperties.delegatesCount!! > confirmedVotes + unconfirmedForVotes
    }

    private fun isAlreadyVoted(senderAddress: String, nodeId: String): Boolean =
        walletVoteService.getVotesByAddress(senderAddress).any { it.id.nodeId == nodeId }

    private fun isAlreadySentVote(senderAddress: String, nodeId: String, voteTypeId: Int): Boolean {
        val unconfirmed = unconfirmedRepository.findAllByHeaderSenderAddress(senderAddress)
        return unconfirmed.any { it.payload.nodeId == nodeId && it.payload.voteTypeId == voteTypeId }
    }

    private fun isValidFee(typeId: Int, fee: Long): Boolean = when {
        typeId == VoteType.FOR.getId() && fee != consensusProperties.feeVoteTxFor!! -> false
        typeId == VoteType.AGAINST.getId() && fee != consensusProperties.feeVoteTxAgainst!! -> false
        else -> true
    }

    private fun isExistsVoteType(typeId: Int): Boolean = VoteType.values().any { it.getId() == typeId }

}