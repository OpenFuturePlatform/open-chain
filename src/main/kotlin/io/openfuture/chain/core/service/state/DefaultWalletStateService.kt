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
    private val unconfirmedTransactionRepository: UTransactionRepository<UnconfirmedTransaction>
) : BaseStateService<WalletState>(repository), WalletStateService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    override fun getBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            return getLastByAddress(address)?.payload?.balance ?: DEFAULT_WALLET_BALANCE
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun getActualBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            val balance = getLastByAddress(address)?.payload?.balance ?: DEFAULT_WALLET_BALANCE
            return balance - getUnconfirmedBalance(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun updateBalanceByAddress(address: String, amount: Long) {
        val newState = WalletStateMessage(address, getCurrentState(address).balance + amount)
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

    private fun getCurrentState(address: String): WalletStateMessage {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address) as? WalletStateMessage
                ?: repository.findLastByAddress(address)?.toMessage()
                ?: WalletStateMessage(address, DEFAULT_WALLET_BALANCE)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}