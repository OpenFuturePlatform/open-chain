package io.openfuture.chain.core.model.entity.transaction.payload

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class VoteTransactionPayload(

    fee: Long,

    @Column(name = "vote_type_id", nullable = false)
    private var voteTypeId: Int,

    @Column(name = "delegate_key", nullable = false)
    var delegateKey: String

): BaseTransactionPayload(fee) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(fee)
        builder.append(voteTypeId)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

}