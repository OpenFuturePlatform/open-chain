package io.openfuture.chain.core.model.entity.transaction.payload

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class DelegateTransactionPayload(

    fee: Long,

    @Column(name = "delegate_key", nullable = false, unique = true)
    var delegateKey: String

) : BaseTransactionPayload(fee) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(fee)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

}