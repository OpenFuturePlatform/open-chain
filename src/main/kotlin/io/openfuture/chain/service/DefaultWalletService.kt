package io.openfuture.chain.service

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.exception.BalanceException
import org.springframework.stereotype.Service

@Service
class DefaultWalletService(
        private val transactionService: DefaultTransactionService
): WalletService {

    override fun getTotalBalance(key: String): Int {
        val sentTransactions = transactionService.getBySenderKey(key)
        val receiveTransactions = transactionService.getByRecipientKey(key)

        val balance = receiveTransactions.sumBy(Transaction::amount) - sentTransactions.sumBy(Transaction::amount)

        if(0 > balance) {
            throw BalanceException("Incorrect negative balance by address: $key")
        }

        return balance
    }

}
