package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.base.BaseModel

interface MessageConverter<T: BaseModel, K> {

    fun fromEntity(entity: T): K

    fun fromMessage(message: K): T

}