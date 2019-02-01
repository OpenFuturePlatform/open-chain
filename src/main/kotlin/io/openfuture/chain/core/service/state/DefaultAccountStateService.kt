package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.AccountStateRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.AccountStateService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.AccountStateMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultAccountStateService(
    private val repository: AccountStateRepository,
    private val statePool: StatePool,
    private val delegateStateService: DefaultDelegateStateService,
    private val unconfirmedTransactionRepository: UTransactionRepository<UnconfirmedTransaction>
) : BaseStateService<AccountState>(repository), AccountStateService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    override fun getBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        return try {
            getLastByAddress(address).balance
        } catch (ex: NotFoundException) {
            DEFAULT_WALLET_BALANCE
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getActualBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        return try {
            val balance = getLastByAddress(address).balance
            return balance - getUnconfirmedBalance(address)
        } catch (ex: NotFoundException) {
            DEFAULT_WALLET_BALANCE
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getVotesForDelegate(delegateKey: String): List<AccountState> = repository.findVotesByDelegateKey(delegateKey)

    override fun updateBalanceByAddress(address: String, amount: Long): AccountStateMessage {
        val state = getCurrentState(address)

        if (null != state.voteFor) {
            delegateStateService.updateRating(state.voteFor!!, amount)
        }

        val newState = AccountStateMessage(address, state.balance + amount, state.voteFor, state.storage)
        statePool.update(newState)
        return newState
    }

    override fun updateVoteByAddress(address: String, delegateKey: String?): AccountStateMessage {
        val state = AccountStateMessage(address, getCurrentState(address).balance, delegateKey)
        statePool.update(state)
        return state
    }

    override fun updateStorage(address: String, storage: String): AccountStateMessage {
        val state = getCurrentState(address)

        val newState = AccountStateMessage(address, state.balance, state.voteFor, storage)
        statePool.update(newState)
        return newState
    }

    private fun getUnconfirmedBalance(address: String): Long =
        unconfirmedTransactionRepository.findAllByHeaderSenderAddress(address).asSequence().map {
            it.header.fee + when (it) {
                is UnconfirmedTransferTransaction -> it.payload.amount
                is UnconfirmedDelegateTransaction -> it.payload.amount
                else -> 0
            }
        }.sum()

    private fun getCurrentState(address: String): AccountStateMessage {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address) as? AccountStateMessage
                ?: repository.findFirstByAddressOrderByBlockIdDesc(address)?.toMessage()
                ?: AccountStateMessage(address, DEFAULT_WALLET_BALANCE)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional
    override fun commit(state: AccountState) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(state)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}