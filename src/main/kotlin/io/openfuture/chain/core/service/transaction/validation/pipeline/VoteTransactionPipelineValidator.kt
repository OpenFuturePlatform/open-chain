package io.openfuture.chain.core.service.transaction.validation.pipeline

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_VOTED_FOR_DELEGATE
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_DELEGATE_KEY
import io.openfuture.chain.core.model.entity.dictionary.VoteType.*
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Scope(SCOPE_PROTOTYPE)
@Transactional(readOnly = true)
class VoteTransactionPipelineValidator(
    private val consensusProperties: ConsensusProperties,
    private val uRepository: UVoteTransactionRepository
) : TransactionPipelineValidator<VoteTransactionPipelineValidator>() {

    fun check(): VoteTransactionPipelineValidator {
        checkHash()
        checkSignature()
        checkSenderAddress()
        checkVoteType()
        checkFee()
        checkDelegate()
        return this
    }

    fun checkNew(): VoteTransactionPipelineValidator {
        check()
        checkActualBalance()
        checkVote()
        return this
    }

    fun checkVoteType(): VoteTransactionPipelineValidator {
        handlers.add {
            it as VoteTransaction
            if (values().none { type -> type.getId() == it.getPayload().voteTypeId }) {
                throw ValidationException("Vote type with id: ${it.getPayload().voteTypeId} is not exists")
            }
        }
        return this
    }

    fun checkFee(): VoteTransactionPipelineValidator {
        handlers.add {
            it as VoteTransaction
            val typeId = it.getPayload().voteTypeId

            val result = when {
                typeId == FOR.getId() && it.fee != consensusProperties.feeVoteTxFor!! -> false
                typeId == AGAINST.getId() && it.fee != consensusProperties.feeVoteTxAgainst!! -> false
                else -> true
            }

            if (!result) {
                throw ValidationException("Incorrect fee")
            }
        }
        return this
    }

    fun checkDelegate(): VoteTransactionPipelineValidator {
        handlers.add {
            it as VoteTransaction
            if (!stateManager.isExistsDelegateByPublicKey(it.getPayload().delegateKey)) {
                throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
            }
        }
        return this
    }

    fun checkVote(): VoteTransactionPipelineValidator {
        handlers.add {
            it as VoteTransaction
            BlockchainLock.readLock.lock()
            try {
                val unconfirmedVote = uRepository.findAllBySenderAddress(it.senderAddress)
                if (unconfirmedVote.isNotEmpty()) {
                    throw ValidationException("Address ${it.senderAddress} has voted invalid", ALREADY_VOTED_FOR_DELEGATE)
                }

                val accountState = stateManager.getLastByAddress<AccountState>(it.senderAddress)
                val result = when (it.getPayload().getVoteType()) {
                    FOR -> null != accountState.voteFor
                    AGAINST -> null == accountState.voteFor || it.getPayload().delegateKey != accountState.voteFor
                }

                if (result) {
                    throw ValidationException("Address ${it.senderAddress} has voted invalid", ALREADY_VOTED_FOR_DELEGATE)
                }
            } finally {
                BlockchainLock.readLock.unlock()
            }
        }
        return this
    }

}