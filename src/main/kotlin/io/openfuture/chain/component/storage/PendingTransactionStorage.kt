package io.openfuture.chain.component.storage

import io.openfuture.chain.domain.transaction.TransactionDto
import org.springframework.stereotype.Component

@Component
class PendingTransactionStorage(
        private var pendingTransactions: MutableList<TransactionDto> = mutableListOf()
) {

    fun getAll(): List<TransactionDto> {
        return this.pendingTransactions.toList()
    }

    fun add(transaction: TransactionDto) {
        this.pendingTransactions.add(transaction)
    }

    fun addAll(transactions: List<TransactionDto>) {
        this.pendingTransactions.addAll(transactions)
    }

    fun remove(transactions: List<TransactionDto>) {
        this.pendingTransactions.removeAll(transactions)
    }

}