package io.openfuture.chain.service

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.exception.BalanceException
import org.springframework.stereotype.Service

@Service
class DefaultWalletService(
        private val transactionService: DefaultTransactionService
): WalletService {

    companion object {
        private const val MINIMAL_AVAILABLE_BALANCE = 0
    }

    override fun getTotalBalance(key: String): Int {
        val sentTransactions = transactionService.getBySenderKey(key)
        val receivedTransactions = transactionService.getByRecipientKey(key)

        val balance = receivedTransactions.sumBy(Transaction::amount) - sentTransactions.sumBy(Transaction::amount)

        if(MINIMAL_AVAILABLE_BALANCE > balance) {
            throw BalanceException("Incorrect negative balance by address: $key")
        }

        return balance
    }

}
