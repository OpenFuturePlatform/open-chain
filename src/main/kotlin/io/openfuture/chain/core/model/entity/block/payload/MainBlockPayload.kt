package io.openfuture.chain.core.model.entity.block.payload

import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.OneToMany

@Embeddable
class MainBlockPayload(
    previousHash: String,
    reward: Long,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
    var transactions: MutableSet<Transaction<BaseTransactionPayload>> = mutableSetOf()

) : BaseBlockPayload(previousHash, reward) {

    override fun getBytes() : ByteArray {
        val builder = StringBuilder()
        builder.append(previousHash)
        builder.append(reward)
        builder.append(merkleHash)
        return builder.toString().toByteArray()
    }

}