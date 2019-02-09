package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_VOTED_FOR_DELEGATE
import io.openfuture.chain.core.model.entity.dictionary.VoteType.*
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.VoteTransactionValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultVoteTransactionValidator(
    private val consensusProperties: ConsensusProperties,
    private val stateManager: StateManager,
    private val uRepository: UVoteTransactionRepository
) : VoteTransactionValidator {

    override fun validateNew(utx: UnconfirmedVoteTransaction) {
        checkVote(utx)
    }

    override fun validate(utx: UnconfirmedVoteTransaction) {
        checkVoteType(utx)
        checkFee(utx)
        checkDelegate(utx)
    }

    private fun checkVoteType(utx: UnconfirmedVoteTransaction) {
        if (values().none { it.getId() == utx.getPayload().voteTypeId }) {
            throw ValidationException("Vote type with id: ${utx.getPayload().voteTypeId} is not exists")
        }
    }

    private fun checkFee(utx: UnconfirmedVoteTransaction) {
        val typeId = utx.getPayload().voteTypeId

        val result = when {
            typeId == FOR.getId() && utx.fee != consensusProperties.feeVoteTxFor!! -> false
            typeId == AGAINST.getId() && utx.fee != consensusProperties.feeVoteTxAgainst!! -> false
            else -> true
        }

        if (!result) {
            throw ValidationException("Incorrect fee")
        }
    }

    private fun checkDelegate(utx: UnconfirmedVoteTransaction) {
        if (!stateManager.isExistsDelegateByPublicKey(utx.getPayload().delegateKey)) {
            throw ValidationException("Incorrect delegate key", ExceptionType.INCORRECT_DELEGATE_KEY)
        }
    }

    private fun checkVote(utx: UnconfirmedVoteTransaction) {
        val unconfirmedVote = uRepository.findAllBySenderAddress(utx.senderAddress)
        if (unconfirmedVote.isNotEmpty()) {
            throw ValidationException("Address ${utx.senderAddress} has voted invalid", ALREADY_VOTED_FOR_DELEGATE)
        }

        val accountState = stateManager.getLastByAddress<AccountState>(utx.senderAddress)
        val result = when (utx.getPayload().getVoteType()) {
            FOR -> null != accountState.voteFor
            AGAINST -> null == accountState.voteFor || utx.getPayload().delegateKey != accountState.voteFor
        }

        if (result) {
            throw ValidationException("Address ${utx.senderAddress} has voted invalid", ALREADY_VOTED_FOR_DELEGATE)
        }
    }

}