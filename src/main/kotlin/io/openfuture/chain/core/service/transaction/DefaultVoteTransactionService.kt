package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.*
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.dictionary.VoteType.FOR
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.VoteTransactionService
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
    private val delegateStateService: DelegateStateService,
    private val consensusProperties: ConsensusProperties
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
    override fun getUnconfirmedBySenderAgainstDelegate(senderAddress: String, delegateKey: String): UnconfirmedVoteTransaction? =
        (unconfirmedRepository as UVoteTransactionRepository)
            .findOneByHeaderSenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeId(senderAddress, delegateKey, AGAINST.getId())

    @Transactional(readOnly = true)
    override fun getLastVoteForDelegate(senderAddress: String, delegateKey: String): VoteTransaction =
        (repository as VoteTransactionRepository)
            .findFirstByHeaderSenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeIdOrderByHeaderTimestampDesc(senderAddress, delegateKey, FOR.getId())
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
    override fun commit(transaction: VoteTransaction): VoteTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val tx = repository.findOneByFooterHash(transaction.footer.hash)
            if (null != tx) {
                return tx
            }

            val utx = unconfirmedRepository.findOneByFooterHash(transaction.footer.hash)
            if (null != utx) {
                return confirm(utx, transaction)
            }

            return this.save(transaction)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun updateState(message: VoteTransactionMessage) {
        when (VoteType.getById(message.voteTypeId)) {
            FOR -> {
                val accountState = accountStateService.updateVoteByAddress(message.senderAddress, message.delegateKey)
                delegateStateService.updateRating(message.delegateKey, accountState.balance)
            }
            AGAINST -> {
                val accountState = accountStateService.updateVoteByAddress(message.senderAddress, null)
                delegateStateService.updateRating(message.delegateKey, -accountState.balance)
            }
        }
        accountStateService.updateBalanceByAddress(message.senderAddress, -message.fee)
    }

    override fun generateReceipt(message: VoteTransactionMessage, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(
                message.senderAddress,
                consensusProperties.genesisAddress!!,
                0,
                "${VoteType.getById(message.voteTypeId)} ${message.delegateKey}"
            ),
            ReceiptResult(
                message.senderAddress,
                delegateWallet,
                message.fee
            )
        )
        return getReceipt(message.hash, results)
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
    override fun save(tx: VoteTransaction): VoteTransaction = super.save(tx)

    override fun validate(utx: UnconfirmedVoteTransaction) {
        super.validate(utx)

        if (!isExistsVoteType(utx.payload.voteTypeId)) {
            throw ValidationException("Vote type with id: ${utx.payload.voteTypeId} is not exists")
        }

        if (!isValidFee(utx.payload.voteTypeId, utx.header.fee)) {
            throw ValidationException("Incorrect fee")
        }

        if (!isExistsDelegate(utx.payload.delegateKey)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }
    }

    @Transactional(readOnly = true)
    override fun validateNew(utx: UnconfirmedVoteTransaction) {
        if (!isValidActualBalance(utx.header.senderAddress, utx.header.fee)) {
            throw ValidationException("Insufficient actual balance", INSUFFICIENT_ACTUAL_BALANCE)
        }

        if (isVoted(utx.header.senderAddress, utx.payload.delegateKey, utx.payload.getVoteType())) {
            throw ValidationException("Address ${utx.header.senderAddress} has voted invalid",
                ALREADY_VOTED_FOR_DELEGATE)
        }
    }

    private fun isExistsDelegate(delegateKey: String): Boolean = delegateStateService.isExistsByPublicKey(delegateKey)

    private fun isVoted(senderAddress: String, delegateKey: String, voteType: VoteType): Boolean {
        val unconfirmedVote = unconfirmedRepository.findAllByHeaderSenderAddress(senderAddress)
        if (unconfirmedVote.isNotEmpty()) {
            return true
        }

        val persistVote = accountStateService.getLastByAddress(senderAddress)
        return when (voteType) {
            FOR -> null != persistVote.voteFor
            AGAINST -> null == persistVote.voteFor || delegateKey != persistVote.voteFor
        }
    }

    private fun isValidFee(typeId: Int, fee: Long): Boolean = when {
        typeId == FOR.getId() && fee != consensusProperties.feeVoteTxFor!! -> false
        typeId == AGAINST.getId() && fee != consensusProperties.feeVoteTxAgainst!! -> false
        else -> true
    }

    private fun isExistsVoteType(typeId: Int): Boolean = VoteType.values().any { it.getId() == typeId }

}