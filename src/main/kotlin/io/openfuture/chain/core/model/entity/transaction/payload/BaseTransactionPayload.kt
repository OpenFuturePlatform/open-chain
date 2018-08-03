package io.openfuture.chain.core.model.entity.transaction.payload

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseTransactionPayload(

    @Column(name = "fee", nullable = false)
    var fee: Long

) {

    abstract fun getBytes(): ByteArray

}