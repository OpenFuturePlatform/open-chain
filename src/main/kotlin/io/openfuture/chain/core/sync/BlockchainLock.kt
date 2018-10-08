package io.openfuture.chain.core.sync

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock

object BlockchainLock {

    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
    val readLock: ReadLock = lock.readLock()
    val writeLock: WriteLock = lock.writeLock()

}