package io.openfuture.chain.component.storage

import io.openfuture.chain.domain.transaction.TransactionDto
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class PendingTransactionStorage {

    private var pendingTransactions: CopyOnWriteArrayList<TransactionDto> = CopyOnWriteArrayList()

    private val lock: ReadWriteLock = ReentrantReadWriteLock()


    fun getAll(): List<TransactionDto> {
        this.lock.readLock().lock()
        try {
            return this.pendingTransactions.toList()
        } finally {
            this.lock.readLock().unlock()
        }
    }

    fun add(transaction: TransactionDto) {
        this.lock.writeLock().lock()
        try {
            this.pendingTransactions.add(transaction)
        } finally {
            this.lock.writeLock().unlock()
        }
    }

    fun addAll(transactions: List<TransactionDto>) {
        this.lock.writeLock().lock()
        try {
            this.pendingTransactions.addAll(transactions)
        } finally {
            this.lock.writeLock().unlock()
        }
    }

    fun remove(transactions: List<TransactionDto>) {
        this.lock.writeLock().lock()
        try {
            this.pendingTransactions.removeAll(transactions)
        } finally {
            this.lock.writeLock().unlock()
        }
    }

}