package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_VOTED_FOR_DELEGATE
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_DELEGATE_KEY
import io.openfuture.chain.core.model.entity.dictionary.VoteType.*
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.VoteTransactionValidator
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultVoteTransactionValidator(
    private val consensusProperties: ConsensusProperties,
    private val stateManager: StateManager,
    private val uRepository: UVoteTransactionRepository
) : VoteTransactionValidator {

    override fun validate(tx: VoteTransaction, new: Boolean) {
        checkVoteType(tx)
        checkFee(tx)
        checkDelegate(tx)
        if (new) {
            checkVote(tx)
        }
    }

    private fun checkVoteType(tx: VoteTransaction) {
        if (values().none { it.getId() == tx.getPayload().voteTypeId }) {
            throw ValidationException("Vote type with id: ${tx.getPayload().voteTypeId} is not exists")
        }
    }

    private fun checkFee(tx: VoteTransaction) {
        val typeId = tx.getPayload().voteTypeId

        val result = when {
            typeId == FOR.getId() && tx.fee != consensusProperties.feeVoteTxFor!! -> false
            typeId == AGAINST.getId() && tx.fee != consensusProperties.feeVoteTxAgainst!! -> false
            else -> true
        }

        if (!result) {
            throw ValidationException("Incorrect fee")
        }
    }

    private fun checkDelegate(tx: VoteTransaction) {
        if (!stateManager.isExistsDelegateByPublicKey(tx.getPayload().delegateKey)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }
    }

    private fun checkVote(tx: VoteTransaction) {
        BlockchainLock.readLock.lock()
        try {
            val unconfirmedVote = uRepository.findAllBySenderAddress(tx.senderAddress)
            if (unconfirmedVote.isNotEmpty()) {
                throw ValidationException("Address ${tx.senderAddress} has voted invalid", ALREADY_VOTED_FOR_DELEGATE)
            }

            val accountState = stateManager.getLastByAddress<AccountState>(tx.senderAddress)
            val result = when (tx.getPayload().getVoteType()) {
                FOR -> null != accountState.voteFor
                AGAINST -> null == accountState.voteFor || tx.getPayload().delegateKey != accountState.voteFor
            }

            if (result) {
                throw ValidationException("Address ${tx.senderAddress} has voted invalid", ALREADY_VOTED_FOR_DELEGATE)
            }
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}