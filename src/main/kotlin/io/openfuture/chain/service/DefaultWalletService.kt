package io.openfuture.chain.service

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.repository.WalletRepository
import org.springframework.stereotype.Service

@Service
class DefaultWalletService(
        private val repository: WalletRepository
) : WalletService {

    companion object {
        private const val DEFAULT_WALLET_BALANCE = 0.0
    }

    override fun updateByTransaction(transaction: Transaction) {
        update(transaction.from, transaction.amount * -1)

        update(transaction.to, transaction.amount)
    }

    override fun getBalance(address: String): Double =
            repository.findOneByAddress(address)?.let(Wallet::balance) ?: DEFAULT_WALLET_BALANCE

    private fun update(address: String, amount: Int) {
        var wallet = repository.findOneByAddress(address)

        wallet = wallet?: Wallet(address)

        wallet.balance += amount

        repository.save(wallet)
    }

}
