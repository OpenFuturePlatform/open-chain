package io.openfuture.chain.core.model.entity.block.payload

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.MappedSuperclass

@Embeddable
@MappedSuperclass
abstract class BaseBlockPayload(

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String,

    @Column(name = "reward", nullable = false)
    var reward: Long

) {

    abstract fun getBytes(): ByteArray

}