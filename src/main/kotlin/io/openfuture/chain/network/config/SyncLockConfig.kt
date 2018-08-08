package io.openfuture.chain.network.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.locks.ReentrantReadWriteLock

@Configuration
class SyncLockConfig {

    @Bean
    fun reentrantReadWriteLock(): ReentrantReadWriteLock = ReentrantReadWriteLock(true)

}
