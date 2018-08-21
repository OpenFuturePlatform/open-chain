package io.openfuture.chain.core.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class BlockchainSynchronized(val throwable: Boolean = false)