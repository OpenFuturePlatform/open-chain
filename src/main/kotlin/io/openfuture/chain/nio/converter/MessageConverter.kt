package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.base.BaseModel

interface MessageConverter<E: BaseModel, M> {

    fun fromEntity(entity: E): M

    fun fromMessage(message: M): E

}