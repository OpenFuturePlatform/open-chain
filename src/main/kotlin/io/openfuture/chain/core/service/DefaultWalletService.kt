package io.openfuture.chain.core.service

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultWalletService(
    private val repository: WalletRepository
) : WalletService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0L
    }


    @Transactional(readOnly = true)
    override fun getByAddress(address: String): Wallet = repository.findOneByAddress(address)
        ?: throw NotFoundException("Wallet with address: $address not found")

    @Transactional(readOnly = true)
    override fun getBalanceByAddress(address: String): Long {
        val wallet = repository.findOneByAddress(address) ?: return DEFAULT_WALLET_BALANCE

        return wallet.balance - wallet.unconfirmedOutput
    }

    @Transactional(readOnly = true)
    override fun getVotesByAddress(address: String): MutableSet<Delegate> = this.getByAddress(address).votes

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

        updateUnconfirmedByAddress(address, -amount)
    }

    @Transactional
    override fun increaseUnconfirmedOutput(address: String, amount: Long) {
        updateUnconfirmedByAddress(address, amount)
    }

    private fun updateByAddress(address: String, amount: Long) {
        val wallet = repository.findOneByAddress(address) ?: Wallet(address)
        wallet.balance += amount
        repository.save(wallet)
    }

    private fun updateUnconfirmedByAddress(address: String, amount: Long) {
        val wallet = repository.findOneByAddress(address) ?: Wallet(address)
        wallet.unconfirmedOutput += amount
        repository.save(wallet)
    }

}
