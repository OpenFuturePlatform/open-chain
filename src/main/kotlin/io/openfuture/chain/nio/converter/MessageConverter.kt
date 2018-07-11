package io.openfuture.chain.nio.converter

interface MessageConverter<T, K> {

    fun fromEntity(entity: T): K

    fun fromMessage(message: K): T

}