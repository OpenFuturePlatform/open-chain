package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.state.WalletState
import io.openfuture.chain.core.model.entity.state.payload.WalletPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.repository.WalletStateRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.WalletStateService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultWalletStateService(
    private val repository: WalletStateRepository,
    private val statePool: StatePool,
    private val blockService: BlockService,
    private val unconfirmedTransactionRepository: UTransactionRepository<UnconfirmedTransaction>
) : BaseStateService<WalletState>(repository), WalletStateService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    override fun getBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            return getLastByAddress(address)?.payload?.data?.balance ?: DEFAULT_WALLET_BALANCE
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getActualBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            val balance = getLastByAddress(address)?.payload?.data?.balance ?: DEFAULT_WALLET_BALANCE
            return balance - getUnconfirmedBalance(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun updateBalanceByAddress(address: String, amount: Long) {
        val state = getCurrentState(address)

        state.payload.data.balance += amount

        val newState = createState(
            address,
            WalletPayload(WalletPayload.Data(state.payload.data.balance + amount, state.payload.data.votes))
        )

        statePool.update(newState)
    }

    override fun getVotesByAddress(address: String): List<String> {
        BlockchainLock.readLock.lock()
        try {
            return getLastByAddress(address)?.payload?.data?.votes ?: emptyList()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun updateVoteByAddress(address: String, nodeId: String, type: VoteType) {
        val state = getCurrentState(address)

        val votes = state.payload.data.votes
        when (type) {
            VoteType.FOR -> votes.add(nodeId)
            VoteType.AGAINST -> votes.remove(nodeId)
        }

        val newState = createState(address, WalletPayload(WalletPayload.Data(state.payload.data.balance, votes)))

        statePool.update(newState)
    }

    private fun getUnconfirmedBalance(address: String): Long =
        unconfirmedTransactionRepository.findAllByHeaderSenderAddress(address).asSequence().map {
            it.header.fee + when (it) {
                is UnconfirmedTransferTransaction -> it.payload.amount
                is UnconfirmedDelegateTransaction -> it.payload.amount
                else -> 0
            }
        }.sum()

    private fun createState(address: String, payload: WalletPayload): WalletState =
        WalletState(address, blockService.getLast().height + 1, payload)

    private fun getCurrentState(address: String): WalletState {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address) as? WalletState
                ?: repository.findLastByAddress(address)
                ?: createState(address, WalletPayload(WalletPayload.Data()))
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}