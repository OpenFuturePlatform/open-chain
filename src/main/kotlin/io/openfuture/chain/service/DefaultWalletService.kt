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


    override fun getBalance(address: String): Double =
        repository.findOneByAddress(address)?.balance ?: DEFAULT_WALLET_BALANCE

    override fun updateByTransaction(transaction: Transaction) {
        updateByAddress(transaction.senderAddress, -transaction.amount)

        updateByAddress(transaction.recipientAddress, transaction.amount)
    }

    private fun updateByAddress(address: String, amount: Int) {
        val wallet = repository.findOneByAddress(address) ?: Wallet(address)

        wallet.balance += amount

        repository.save(wallet)
    }

}
