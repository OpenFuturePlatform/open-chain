package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.repository.AccountStateRepository
import io.openfuture.chain.core.service.AccountStateService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultAccountStateService(
    private val repository: AccountStateRepository,
    private val statePool: StatePool
) : DefaultStateService<AccountState>(repository), AccountStateService {

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

    override fun getVotesForDelegate(delegateKey: String): List<AccountState> = repository.findAllByVoteFor(delegateKey)

    override fun updateBalanceByAddress(address: String, amount: Long): AccountState {
        val state = getCurrentState(address)
        state.balance += amount
        statePool.update(state)
        return state
    }

    override fun updateVoteByAddress(address: String, delegateKey: String?): AccountState {
        val state = getCurrentState(address)
        state.voteFor = delegateKey
        statePool.update(state)
        return state
    }

    override fun updateStorage(address: String, storage: String): AccountState {
        val state = getCurrentState(address)
        state.storage = storage
        statePool.update(state)
        return state
    }

    private fun getCurrentState(address: String): AccountState {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address) as? AccountState
                ?: repository.findOneByAddress(address)
                ?: AccountState(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    private fun getLastByAddress(address: String): AccountState =
        repository.findOneByAddress(address) ?: throw NotFoundException("Account state with address $address not found")

}