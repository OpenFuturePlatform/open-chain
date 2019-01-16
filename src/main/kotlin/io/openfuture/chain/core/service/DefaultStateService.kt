package io.openfuture.chain.core.service

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.State
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.StateRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultStateService(
    private val repository: StateRepository,
    private val statePool: StatePool,
    private val unconfirmedTransactionRepository: UTransactionRepository<UnconfirmedTransaction>,
    private val blockService: BlockService
) : StateService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    override fun getLastByAddress(address: String): State? {
        BlockchainLock.readLock.lock()
        try {
            return repository.findLastByAddress(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getByAddress(address: String): List<State> {
        BlockchainLock.readLock.lock()
        try {
            return repository.findByAddress(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getByAddressAndBlock(address: String, block: Block): State? {
        BlockchainLock.readLock.lock()
        try {
            return repository.findByAddressAndBlock(address, block)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            return getLastByAddress(address)?.data?.balance ?: DEFAULT_WALLET_BALANCE
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun increaseBalance(address: String, amount: Long) {
        updateBalanceByAddress(address, amount)
    }

    override fun decreaseBalance(address: String, amount: Long) {
        updateBalanceByAddress(address, -amount)
    }

    override fun getActualBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            val balance = getLastByAddress(address)?.data?.balance ?: DEFAULT_WALLET_BALANCE
            return balance - getUnconfirmedBalance(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getVotesByAddress(address: String): List<String> {
        BlockchainLock.readLock.lock()
        try {
            return getLastByAddress(address)?.data?.votes ?: emptyList()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun addVote(address: String, nodeId: String) {
        val state = getCurrentState(address)
        state.data.votes.add(nodeId)
        statePool.update(state)
    }

    override fun removeVote(address: String, nodeId: String) {
        val state = getCurrentState(address)
        state.data.votes.remove(nodeId)
        statePool.update(state)
    }

    @Transactional
    override fun create(state: State): State = repository.save(state)

    private fun getUnconfirmedBalance(address: String): Long =
        unconfirmedTransactionRepository.findAllByHeaderSenderAddress(address).asSequence().map {
            it.header.fee + when (it) {
                is UnconfirmedTransferTransaction -> it.payload.amount
                is UnconfirmedDelegateTransaction -> it.payload.amount
                else -> 0
            }
        }.sum()


    private fun updateBalanceByAddress(address: String, amount: Long) {
        val state = getCurrentState(address)
        state.data.balance += amount
        statePool.update(state)
    }

    private fun getCurrentState(address: String): State {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address)
                ?: repository.findLastByAddress(address)
                ?: State(address, State.Data(), blockService.getLast())
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}