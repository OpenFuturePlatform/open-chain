package io.openfuture.chain.core.model.entity.transaction.payload

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class TransferTransactionPayload(

    fee: Long,

    @Column(name = "amount", nullable = false)
    var amount: Long,

    @Column(name = "recipient_address", nullable = false)
    var recipientAddress: String

) : BaseTransactionPayload(fee) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(fee)
        builder.append(recipientAddress)
        return builder.toString().toByteArray()
    }

}