package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_VOTED_FOR_DELEGATE
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_DELEGATE_KEY
import io.openfuture.chain.core.model.entity.dictionary.VoteType.*
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.core.util.TransactionValidateHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class VoteTransactionValidator(
    private val consensusProperties: ConsensusProperties,
    private val uRepository: UVoteTransactionRepository
) : TransactionValidator() {

    fun check(): Array<TransactionValidateHandler> = arrayOf(
        checkHash(),
        checkSignature(),
        checkSenderAddress(),
        checkVoteType(),
        checkFee(),
        checkDelegate()
    )

    fun checkNew(unconfirmedBalance: Long): Array<TransactionValidateHandler> = arrayOf(
        *check(),
        checkActualBalance(unconfirmedBalance),
        checkVote()
    )

    fun checkVoteType(): TransactionValidateHandler = {
        it as VoteTransaction
        if (values().none { type -> type.getId() == it.getPayload().voteTypeId }) {
            throw ValidationException("Vote type with id: ${it.getPayload().voteTypeId} is not exists")
        }
    }

    fun checkFee(): TransactionValidateHandler = {
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

    fun checkDelegate(): TransactionValidateHandler = {
        it as VoteTransaction
        if (!stateManager.isExistsDelegateByPublicKey(it.getPayload().delegateKey)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }
    }

    fun checkVote(): TransactionValidateHandler = {
        it as VoteTransaction
        BlockchainLock.readLock.lock()
        try {
            val unconfirmedVote = uRepository.findAllBySenderAddress(it.senderAddress)
            if (unconfirmedVote.isNotEmpty()) {
                throw ValidationException("Address ${it.senderAddress} has voted invalid", ALREADY_VOTED_FOR_DELEGATE)
            }

            val accountState = stateManager.getByAddress<AccountState>(it.senderAddress)
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

}