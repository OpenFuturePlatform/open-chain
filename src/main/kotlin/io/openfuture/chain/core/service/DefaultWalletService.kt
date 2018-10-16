package io.openfuture.chain.core.service

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.repository.WalletRepository
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultWalletService(
    private val repository: WalletRepository,
    private val unconfirmedTransactionRepository: UTransactionRepository<UnconfirmedTransaction>
) : WalletService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    @Transactional(readOnly = true)
    override fun getByAddress(address: String): Wallet {
        BlockchainLock.readLock.lock()
        try {
            return repository.findOneByAddress(address)
                ?: throw NotFoundException("Wallet with address: $address not found")
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional(readOnly = true)
    override fun getActualBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            val wallet = repository.findOneByAddress(address) ?: return DEFAULT_WALLET_BALANCE
            return wallet.balance - getUnconfirmedBalance(address)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional(readOnly = true)
    override fun getBalanceByAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            val wallet = repository.findOneByAddress(address) ?: return DEFAULT_WALLET_BALANCE
            return wallet.balance
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional
    override fun save(wallet: Wallet) {
        repository.save(wallet)
    }

    @Transactional
    override fun increaseBalance(address: String, amount: Long) {
        updateByAddress(address, amount)
    }

    @Transactional
    override fun decreaseBalance(address: String, amount: Long) {
        updateByAddress(address, -amount)
    }

    private fun updateByAddress(address: String, amount: Long) {
        BlockchainLock.writeLock.lock()
        try {
            val wallet = repository.findOneByAddress(address) ?: Wallet(address)
            wallet.balance += amount
            repository.save(wallet)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    private fun getUnconfirmedBalance(address: String): Long =
        unconfirmedTransactionRepository.findAllByHeaderSenderAddress(address).asSequence().map {
            it.header.fee + when (it) {
                is UnconfirmedTransferTransaction -> it.payload.amount
                is UnconfirmedDelegateTransaction -> it.payload.amount
                else -> 0
            }
        }.sum()

}
