package io.openfuture.chain.network.component.time

import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class Clock {

    @Volatile private var offset: Long = 0

    private val lock: ReadWriteLock = ReentrantReadWriteLock()


    fun currentTimeMillis(): Long {
        lock.readLock().lock()
        try {
            return System.currentTimeMillis().plus(offset)
        } finally {
            lock.readLock().unlock()
        }
    }

    fun adjust(offset: Long) {
        lock.writeLock().lock()
        try {
            this.offset = offset
        } finally {
            lock.writeLock().unlock()
        }
    }

}