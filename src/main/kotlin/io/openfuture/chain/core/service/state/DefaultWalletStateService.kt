package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.state.WalletState
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.repository.WalletStateRepository
import io.openfuture.chain.core.service.WalletStateService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.WalletStateMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultWalletStateService(
    private val repository: WalletStateRepository,
    private val statePool: StatePool,
    private val delegateStateService: DefaultDelegateStateService,
    private val unconfirmedTransactionRepository: UTransactionRepository<UnconfirmedTransaction>
) : BaseStateService<WalletState>(repository), WalletStateService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    override fun getBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            return getLastByAddress(address)?.balance ?: DEFAULT_WALLET_BALANCE
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getActualBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            val balance = getLastByAddress(address)?.balance ?: DEFAULT_WALLET_BALANCE
            return balance - getUnconfirmedBalance(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getVotesForDelegate(delegateKey: String): List<WalletState> = repository.findAllByVoteFor(delegateKey)

    override fun updateBalanceByAddress(address: String, amount: Long): WalletStateMessage {
        val walletState = getCurrentState(address)

        if (null != walletState.voteFor) {
            delegateStateService.updateRating(walletState.voteFor!!, amount)
        }

        val newWalletState = WalletStateMessage(address, walletState.balance + amount, walletState.voteFor)
        statePool.update(newWalletState)
        return newWalletState
    }

    override fun updateVoteByAddress(address: String, delegateKey: String?): WalletStateMessage {
        val newWalletState = WalletStateMessage(address, getCurrentState(address).balance, delegateKey)
        statePool.update(newWalletState)
        return newWalletState
    }

    private fun getUnconfirmedBalance(address: String): Long =
        unconfirmedTransactionRepository.findAllByHeaderSenderAddress(address).asSequence().map {
            it.header.fee + when (it) {
                is UnconfirmedTransferTransaction -> it.payload.amount
                is UnconfirmedDelegateTransaction -> it.payload.amount
                else -> 0
            }
        }.sum()

    private fun getCurrentState(address: String): WalletStateMessage {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address) as? WalletStateMessage
                ?: repository.findFirstByAddressOrderByBlockIdDesc(address)?.toMessage()
                ?: WalletStateMessage(address, DEFAULT_WALLET_BALANCE)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional
    override fun commit(state: WalletState) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(state)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}